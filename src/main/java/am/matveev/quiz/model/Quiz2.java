package am.matveev.quiz.model;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class Quiz2{

    private List<Question> questions2;

    public Quiz2() {
        questions2 = new ArrayList<>();
        initializeQuestions();
    }

    private void initializeQuestions() {

        List<String> answers11 = Arrays.asList("28 кг", "22 кг", "30 кг", "10 кг");
        List<String> answers12 = Arrays.asList("Бедренная", "Локтевая", "Большеберцовая", "Плечевая");
        List<String> answers13 = Arrays.asList("До 11", "До 35", "До 22", "До 28");
        List<String> answers14 = Arrays.asList("Больше 500", "Больше 200", "Больше 400", "Больше 600");
        List<String> answers15 = Arrays.asList("От 16 Гц до 122 кГц", "От 10 Гц до 12 кГц", "От 46 Гц до 50 кГц", "От 100 Гц до 200 кГц");
        List<String> answers16 = Arrays.asList("Более 90 квадратных метров", "Более 10 квадратных метров", "Более 120 квадратных метров", "Более 150 квадратных сантиметров");
        List<String> answers17 = Arrays.asList("Более 20", "Более 100", "Более 200", "Более 400");
        List<String> answers18 = Arrays.asList("Примерно 10 000 км", "Примерно 100 000 км", "Примерно 200 000 км", "Примерно 1000 км");
        List<String> answers19 = Arrays.asList("Около 3 квадратных метров", "Около 2 квадратных метров", "Около 5 квадратных метров", "Около 4 квадратных метров");
        List<String> answers20 = Arrays.asList("Икроножная", "Большая ягодичная", "Широчайшая мышца", "Бедренная");

        questions2.add(new Question("1. Сколько в среднем весят мышцы человека?", answers11, "28 кг"));
        questions2.add(new Question("2. Какая кость в скелете - самая длинная?", answers12, "Бедренная"));
        questions2.add(new Question("3. Сколько процентов воды содержит кость?", answers13, "До 22"));
        questions2.add(new Question("4. Сколько мышц в теле человека?", answers14, "Больше 600"));
        questions2.add(new Question("5. Какой диапазон частот способно улавливать ухо человека?", answers15, "От 16 Гц до 122 кГц"));
        questions2.add(new Question("6. Какова площадь дыхательной поверхности легких??", answers16, "Более 90 квадратных метров"));
        questions2.add(new Question("7. Сколько костей в скелете человека?", answers17, "Более 200"));
        questions2.add(new Question("8. Какова средняя длина всех сосудов человека?", answers18, "Примерно 100 000 км"));
        questions2.add(new Question("9. Какую площадь имеет кожа человека?", answers19, "Около 2 квадратных метров"));
        questions2.add(new Question("10. Какая мышца в теле человека является самой крупной?", answers20, "Большая ягодичная"));
    }


    public List<Question> getQuestions() {
        return questions2;
    }
}
