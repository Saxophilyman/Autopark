document.addEventListener("DOMContentLoaded", function () {
    let mapElement = document.getElementById("map");
    if (!mapElement) {
        console.error("Ошибка: контейнер карты не найден!");
        return;
    }

    let map = L.map("map").setView([55.751244, 37.618423], 12);

    // L.tileLayer("https://api.maptiler.com/maps/streets/{z}/{x}/{y}.png?key=V2pOsG04veMYkRuJR4aP", {
    //     attribution: '&copy; <a href="https://www.maptiler.com/">MapTiler</a> contributors'
    // }).addTo(map);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);


    setTimeout(() => {
        map.invalidateSize();
    }, 500);

    // Функция установки даты (за 7 дней по умолчанию)
    function setDefaultDates() {
        let today = new Date();
        let lastWeek = new Date();
        lastWeek.setDate(today.getDate() - 7);

        document.getElementById("fromDate").value = lastWeek.toISOString().split("T")[0];
        document.getElementById("toDate").value = today.toISOString().split("T")[0];
    }

    setDefaultDates(); // Автоустановка дат при загрузке

    window.loadTrips = function () {
        let vehicleId = document.body.dataset.vehicleId;
        let fromDate = document.getElementById("fromDate").value;
        let toDate = document.getElementById("toDate").value;

        if (!vehicleId) {
            console.error("Ошибка: vehicleId не найден");
            return;
        }

        axios.get(`/api/managers/getOnlyTripsUI`, {
            params: { vehicleId, startTripDate: fromDate, endTripDate: toDate }
        }).then(response => {
            let trips = response.data;
            let tableBody = document.querySelector("#tripsTable tbody");
            tableBody.innerHTML = "";

            if (trips.length === 0) {
                tableBody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">Нет поездок за выбранный период</td></tr>`;
                return;
            }

            trips.forEach(trip => {
                let row = `<tr>
                    <td>${trip.id}</td>
                    <td>${formatDate(trip.startDate)}</td>
                    <td>${formatDate(trip.endDate)}</td>
                    <td>${trip.startLocationInString}</td>
                    <td>${trip.endLocationInString}</td>
                    <td>${trip.duration}</td>
                    <td>
                        <button class="btn btn-sm btn-info" onclick="loadTrack(${trip.id})">Показать трек</button>
                    </td>
                </tr>`;
                tableBody.innerHTML += row;
            });

            // После загрузки поездок, загружаем все точки
            loadAllTripsData(vehicleId, fromDate, toDate);
        }).catch(error => {
            console.error("Ошибка загрузки поездок:", error);
            alert("Ошибка загрузки поездок. Попробуйте позже.");
        });
    };

    function loadAllTripsData(vehicleId, fromDate, toDate) {
        axios.get(`/api/managers/getOnlyTripsUI`, {
            params: { vehicleId, startTripDate: fromDate, endTripDate: toDate }
        }).then(responseTrips => {
            let trips = responseTrips.data;
            if (trips.length === 0) {
                console.warn("Нет поездок за выбранный период.");
                return;
            }

            axios.get(`/api/managers/trips`, {
                params: { vehicleId, startTripDate: fromDate, endTripDate: toDate }
            }).then(responseGps => {
                let trackPoints = responseGps.data;

                if (trackPoints.length === 0) {
                    console.warn("Нет GPS-данных для поездок.");
                    return;
                }

                let groupedTracks = {};

                trips.forEach(trip => {
                    groupedTracks[trip.id] = trackPoints.filter(point => {
                        let pointTime = new Date(point.timestamp).getTime();
                        let tripStartTime = new Date(trip.startDate).getTime();
                        let tripEndTime = new Date(trip.endDate).getTime();
                        return pointTime >= tripStartTime && pointTime <= tripEndTime;
                    }).map(p => [p.latitude, p.longitude]);
                });

                window.allTracksData = groupedTracks;

                // Показываем кнопку "Показать все треки"
                document.getElementById("showAllTracksButton").style.display = "block";
            }).catch(error => {
                console.error("Ошибка загрузки GPS-данных:", error);
                alert("Ошибка загрузки GPS-данных.");
            });
        }).catch(error => {
            console.error("Ошибка загрузки поездок:", error);
            alert("Ошибка загрузки поездок.");
        });
    }



    window.loadAllTracks = function () {
        if (!window.allTracksData) {
            alert("Сначала загрузите поездки!");
            return;
        }

        if (window.currentTracks) {
            window.currentTracks.forEach(track => track.remove());
        }
        window.currentTracks = [];

        let colors = ["red", "blue", "green", "purple", "orange", "brown"];
        let colorIndex = 0;

        for (let tripId in window.allTracksData) {
            let coordinates = window.allTracksData[tripId];

            if (coordinates.length > 1) {
                let color = colors[colorIndex % colors.length];

                let polyline = L.polyline(coordinates, { color: color, weight: 4 }).addTo(map);
                window.currentTracks.push(polyline);

                // Ставим маркер в центр маршрута с ID поездки
                let midPoint = coordinates[Math.floor(coordinates.length / 2)];
                let marker = L.marker(midPoint).addTo(map)
                    .bindPopup(`Поездка ID: ${tripId}`)
                    .openPopup();

                colorIndex++;
            }
        }

        if (window.currentTracks.length > 0) {
            let group = new L.featureGroup(window.currentTracks);
            map.fitBounds(group.getBounds());
        }
    };





    // Функция загрузки трека
    window.loadTrack = function (tripId) {
        axios.get(`/api/managers/trip-track`, { params: { tripId } })
            .then(response => {
                let trackPoints = response.data;

                if (trackPoints.length === 0) {
                    alert("Для данной поездки нет GPS-данных.");
                    return;
                }

                let coordinates = trackPoints.map(p => [p.latitude, p.longitude]);

                // Очищаем старый трек перед отрисовкой нового
                if (window.currentTrack) {
                    window.currentTrack.remove();
                }

                // Рисуем трек на карте
                window.currentTrack = L.polyline(coordinates, { color: "red" }).addTo(map);
                map.fitBounds(window.currentTrack.getBounds());
            })
            .catch(error => {
                console.error("Ошибка загрузки трека:", error);
                alert("Ошибка загрузки трека.");
            });
    };

    function formatDate(dateTimeString) {
        let date = new Date(dateTimeString);
        return date.toLocaleString("ru-RU", { timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone });
    }
});
