document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("previewForm");

    if (!form) return;

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        const vehicleId = document.getElementById("vehicleId").value;
        const from = document.getElementById("fromPreview").value;
        const to = document.getElementById("toPreview").value;

        const url = `/api/managers/export/vehicle/${vehicleId}?fromDate=${from}&toDate=${to}&format=json`;

        fetch(url)
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Ошибка при получении данных");
                }
                return response.json();
            })
            .then((data) => {
                document.getElementById("jsonContent").textContent = JSON.stringify(data, null, 2);
                document.getElementById("jsonModal").style.display = "block";
            })
            .catch((error) => {
                alert("Не удалось получить данные: " + error.message);
            });
    });
});
