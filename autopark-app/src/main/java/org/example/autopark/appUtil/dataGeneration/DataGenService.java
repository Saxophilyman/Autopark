package org.example.autopark.appUtil.dataGeneration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.autopark.entity.Brand;
import org.example.autopark.entity.Driver;
import org.example.autopark.entity.Enterprise;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.service.BrandsService;
import org.example.autopark.service.DriverService;
import org.example.autopark.service.EnterpriseService;
import org.example.autopark.service.VehicleService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Profile("!reactive")
@Transactional(readOnly = true)
public class DataGenService {
//    private final Set<String> existingLicensePlates;
//    private final Set<String> generatedLicensePlates = new HashSet<>();

    private final EnterpriseService enterpriseService;
    private final BrandsService brandService;
    private final DriverService driverService;
    private final VehicleService vehicleService;


    public DataGenService(EnterpriseService enterprisesService, BrandsService brandsService,
                          DriverService driversService, VehicleService vehiclesService) {
        this.enterpriseService = enterprisesService;
        this.brandService = brandsService;
        this.driverService = driversService;
        this.vehicleService = vehiclesService;
//        this.existingLicensePlates = new HashSet<>(
//                vehicleService.findAll().stream()
//                        .map(Vehicle::getLicensePlate)
//                        .filter(Objects::nonNull)
//                        .collect(Collectors.toSet())
//        );
    }
    @Transactional
    public void generate(DataGenDTO request) {
        for (Long enterpriseId : request.getEnterprisesID()) {
            Enterprise enterprise = enterpriseService.findOne(enterpriseId);
            List<Vehicle> vehicles = generateVehicles(enterprise, request.getNumberOfVehicle());
            List<Driver> drivers = generateDrivers(enterprise, request.getNumberOfDriver());

            assignVehicleWithDriver(vehicles, drivers, request.getIndicatorOfActiveVehicle());
            driverService.saveAll(drivers);
            vehicleService.saveAll(vehicles);
        }
    }

    //ГЕНЕРАЦИЯ VEHICLE
    private List<Vehicle> generateVehicles(Enterprise enterprise, int numberOfVehicle) {
        List<Vehicle> vehicles = new ArrayList<>();

        for (int i = 0; i < numberOfVehicle; i++) {
            Vehicle newRandomVehicle = new Vehicle();
            newRandomVehicle.setEnterpriseOwnerOfVehicle(enterprise);
            //Просто для быстроты, потом переделать с логикой, что несколько брэндов м.б. на выбор
            //в зависимости от id предприятия
            newRandomVehicle.setBrandOwner(genBrandOwner(enterprise.getName()));
            //тоже набросок, который впоследствии нужно оптимизировать
            newRandomVehicle.setVehicleName(generateVehicleName(newRandomVehicle.getBrandOwner().getBrandName()));//Генерация имени. Если принадлежит данному брэнду, то имена следующие
//            newRandomVehicle.setLicensePlate(generateUniqueVehicleLicensePlate());
            newRandomVehicle.setVehicleCost(generateVehicleCost());//цена не менее 0
            newRandomVehicle.setVehicleYearOfRelease(generateVehicleYearOfRelease());

            vehicles.add(newRandomVehicle);
        }
        return vehicles;
    }

    private Brand genBrandOwner(String enterpriseName) {
        switch (enterpriseName) {
            case "BMW AG" -> {
                return brandService.findOne(3L);
            }
            case "Автоваз" -> {
                return brandService.findOne(1L);
            }
            case "Toyota Motor Corporation" -> {
                return brandService.findOne(2L);
            }
            default -> {
                // Логика для случаев, если имя не совпало ни с одним из кейсов
                throw new IllegalArgumentException("Unknown enterprise name: " + enterpriseName);
            }
        }
    }

    //на быструю проверку
    private String generateVehicleName(@NotBlank String brandName) {
        switch (brandName) {
            case "BMW" -> {
                return genForBmwName();
            }
            case "Lada" -> {
                return genForAvtovazName();
            }
            case "Toyota" -> {
                return genForToyotaName();
            }
            default -> {
                // Логика для случаев, если имя не совпало ни с одним из кейсов
                throw new IllegalArgumentException("Unknown brand name: " + brandName);
            }
        }
    }

//    private String generateUniqueVehicleLicensePlate() {
//        String plate;
//        int attempts = 0;
//        do {
//            plate = generateVehicleLicensePlate();
//            attempts++;
//            if (attempts > 1000) {
//                throw new RuntimeException("Не удалось сгенерировать уникальный номер автомобиля");
//            }
//        } while (existingLicensePlates.contains(plate) || generatedLicensePlates.contains(plate));
//
//        generatedLicensePlates.add(plate);
//        return plate;
//    }

    private String generateVehicleLicensePlate() {
        String letters = "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯ";
        Random random = new Random();

        char firstLetter = letters.charAt(random.nextInt(letters.length()));
        String digits = String.format("%03d", random.nextInt(1000));
        char secondLetter = letters.charAt(random.nextInt(letters.length()));
        char thirdLetter = letters.charAt(random.nextInt(letters.length()));

        return "" + firstLetter + digits + secondLetter + thirdLetter;
    }

    private String getRandomElement(String[] array) {
        return array[new Random().nextInt(array.length)];
    }

    private String genForBmwName() {
        return getRandomElement(new String[]{"118i", "220i", "M240i xDrive", "330i", "520i", "X6", "X5",
                "Z4", "iX", "i7", "M5", "M3", "M240i xDrive", "330i", "520i", "X6", "X5"});
    }

    private String genForAvtovazName() {
        return getRandomElement(new String[]{"XRAY Cross", "Niva Travel", "Vesta Cross",
                "Granta Cross", "Granta Drive Active", "Granta Classic", "Vesta NG", "Niva Legend"});
    }

    private String genForToyotaName() {
        return getRandomElement(new String[]{"Corolla", "Camry", "Avalon", "Yaris", "Auris", "RAV4", "Highlander",
                "Corolla", "Land Cruiser 300", "Hilux", "Alphard"});
    }

    private @NotNull @Min(value = 0) int generateVehicleCost() {
        return generateRandomInt(100000, 1000000);
    }

    private int generateVehicleYearOfRelease() {
        return generateRandomInt(2018, 2024);
    }

    private int generateRandomInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    //ГЕНЕРАЦИЯ DRIVER
    private List<Driver> generateDrivers(Enterprise enterprise, int numberOfDriver) {
        List<Driver> drivers = new ArrayList<>();
        for (int i = 0; i < numberOfDriver; i++) {
            Driver newRandomDriver = new Driver();
            newRandomDriver.setEnterpriseOwnerOfDriver(enterprise);
            newRandomDriver.setDriverName(generateDriverName());
            newRandomDriver.setDriverSalary(generateDriverSalary());

            drivers.add(newRandomDriver);
        }
        return drivers;
    }

    private @NotNull @Min(value = 1) int generateDriverSalary() {
        return generateRandomInt(10000, 300000);
    }

    private String generateDriverName() {

        String[] names = {"А.", "Б.", "В.", "Г.", "Д.", "Е.", "И.", "К.", "Л.", "М.", "Н.", "О.", "П."
                , "Р.", "С.", "Т.", "Ф.", "Э.", "Ю.", "Я."};
        String[] patronymics = {"А.", "Б.", "В.", "Г.", "Д.", "Е.", "И.", "К.", "Л.", "М.", "Н.", "О.", "П."
                , "Р.", "С.", "Т.", "Ф.", "Э.", "Ю.", "Я."};
        Random random = new Random();

        String surname = generateSurnames();
        String name = names[random.nextInt(names.length)];
        String patronymic = patronymics[random.nextInt(patronymics.length)];

        return String.format("%s  %s %s", surname, name, patronymic);
    }

    private static String generateSurnames(){
        final String[] surnames = {"Петр",  "Бобр", "Пят", "Жаб", "Крыш", "Малыш", "Смород", "Дур", "Рис",
                "Клюк", "Завод", "Вод", "Зад", "Пет", "Бег", "Пожар", "Крас", "Красн", "Трус", "Карапуз",
                "Струн", "Пуз", "Крыл", "Иван", "Вас", "Дмитр", "Куз", "Кузнец", "Гончар", "Мельник", "Волк", "Сокол",
                "Грач", "Гор", "Озер", "Бел", "Черн", "Добр", "Сибир", "Герц", "Розен", "Казан", "Роман", "Полян",
                "Ворон", "Гав", "Скуп", "Плот", "Скрип", "Заруб", "Сильн", "Сил", "Дум", "Бум", "Фетр",
                "Ветр","Сол","Лис", "Медвед", "Орл", "Зуб", "Мутн", "Трут", "Болт", "Зонт", "Мыш", "Банан",
                "Креп", "Залом", "Жад", "Жид", "Груш", "Пушк", "Гриш", "Лом", "Кит", "Сом", "Том", "Книг",
                "Баб", "Раб", "Рыб", "Мотор", "Ротор", "Слав", "Крик", "Гриб", "Бойк", "Ран", "Жар", "Пан",
                "Дом", "Мох", "Мот", "Пот", "Топ", "Стар", "Сереб", "Облом", "Сигар", "Свет", "Рак", "Помидор",
                "Лук", "Зерн", "Лад", "Волос", "Голос", "Огур", "Бык", "Стакан", "Сметан", "Пул", "Сып", "Дот"
        };

        final String[] suffixes = {
                "ов", "овский", "енко", "ников", "ский", "ин", "кин", "чиков", "арев", "айкин", "ойкин", "ович",
                "овец", "чанин","унов", "ман", "штейн", "ищев", "ухин", "чук", "оват", "ко", "ченко"
        };

        Random random = new Random();

        String root = surnames[random.nextInt(surnames.length)];
        String suffix = suffixes[random.nextInt(suffixes.length)];

        if (random.nextInt(4) == 0) {
            return root + "о" + root.toLowerCase() + suffix;
        } else {
            return root + suffix;
        }
    }

    private Driver findIsActiveDriver(List<Driver> drivers) {
        for (Driver driver : drivers) {
            if (!driver.isActive()) {
                return driver;
            }
        }
        return null; // или создать исключение (?)
    }

    private void assignVehicleWithDriver(List<Vehicle> vehicles,
                                         List<Driver> drivers, int indicatorOfActiveVehicle) {

        int numberActiveVehicles = vehicles.size() / indicatorOfActiveVehicle;

        for (int i = 0; i < numberActiveVehicles; i++) {
            Driver driver = findIsActiveDriver(drivers);
            if (driver != null) {
                Vehicle vehicle = vehicles.get(i * indicatorOfActiveVehicle);
                vehicle.setActiveDriver(driver);
                driver.setActive(true);
                driver.setActiveVehicle(vehicle);
            }
        }
    }
}
