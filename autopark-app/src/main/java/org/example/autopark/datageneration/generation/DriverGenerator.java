package org.example.autopark.datageneration.generation;

import org.example.autopark.datageneration.model.DriverDraft;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("!reactive")
public class DriverGenerator {

    private static final int MIN_SALARY = 10_000;
    private static final int MAX_SALARY = 300_000;

    private static final List<String> INITIALS = List.of(
            "А.", "Б.", "В.", "Г.", "Д.", "Е.", "И.", "К.", "Л.", "М.",
            "Н.", "О.", "П.", "Р.", "С.", "Т.", "Ф.", "Э.", "Ю.", "Я."
    );

    private static final List<String> SURNAME_ROOTS = List.of(
            "Петр", "Бобр", "Пят", "Жаб", "Крыш", "Малыш", "Смород", "Дур", "Рис",
            "Клюк", "Завод", "Вод", "Зад", "Пет", "Бег", "Пожар", "Крас", "Красн",
            "Трус", "Карапуз", "Струн", "Пуз", "Крыл", "Иван", "Вас", "Дмитр", "Куз",
            "Кузнец", "Гончар", "Мельник", "Волк", "Сокол", "Грач", "Гор", "Озер",
            "Бел", "Черн", "Добр", "Сибир", "Герц", "Розен", "Казан", "Роман", "Полян",
            "Ворон", "Гав", "Скуп", "Плот", "Скрип", "Заруб", "Сильн", "Сил", "Дум",
            "Бум", "Фетр", "Ветр", "Сол", "Лис", "Медвед", "Орл", "Зуб", "Мутн", "Трут",
            "Болт", "Зонт", "Мыш", "Банан", "Креп", "Залом", "Жад", "Жид", "Груш", "Пушк",
            "Гриш", "Лом", "Кит", "Сом", "Том", "Книг", "Баб", "Раб", "Рыб", "Мотор",
            "Ротор", "Слав", "Крик", "Гриб", "Бойк", "Ран", "Жар", "Пан", "Дом", "Мох",
            "Мот", "Пот", "Топ", "Стар", "Сереб", "Облом", "Сигар", "Свет", "Рак",
            "Помидор", "Лук", "Зерн", "Лад", "Волос", "Голос", "Огур", "Бык", "Стакан",
            "Сметан", "Пул", "Сып", "Дот"
    );

    private static final List<String> SURNAME_SUFFIXES = List.of(
            "ов", "овский", "енко", "ников", "ский", "ин", "кин", "чиков", "арев",
            "айкин", "ойкин", "ович", "овец", "чанин", "унов", "ман", "штейн", "ищев",
            "ухин", "чук", "оват", "ко", "ченко"
    );

    private final Random random = new Random();

    public List<DriverDraft> generate(int driverCount) {
        if (driverCount < 0) {
            throw new IllegalArgumentException("Число водителей не может быть отрицательным");
        }

        List<DriverDraft> result = new ArrayList<>(driverCount);
        for (int i = 0; i < driverCount; i++) {
            result.add(new DriverDraft(
                    generateDriverName(),
                    randomIntInclusive(MIN_SALARY, MAX_SALARY)
            ));
        }
        return List.copyOf(result);
    }

    private String generateDriverName() {
        String surname = generateSurname();
        String nameInitial = randomElement(INITIALS);
        String patronymicInitial = randomElement(INITIALS);
        return "%s %s %s".formatted(surname, nameInitial, patronymicInitial);
    }

    private String generateSurname() {
        String root = randomElement(SURNAME_ROOTS);
        String suffix = randomElement(SURNAME_SUFFIXES);

        if (random.nextInt(4) == 0) {
            return root + "о" + root.toLowerCase() + suffix;
        }
        return root + suffix;
    }

    private String randomElement(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }

    private int randomIntInclusive(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }
}
