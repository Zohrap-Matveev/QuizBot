package am.matveev.quiz.model;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class Quiz{

    private List<Question> questions;

    public Quiz() {
        questions = new ArrayList<>();
        initializeQuestions();
    }

private void initializeQuestions() {
    List<String> answers1 = Arrays.asList("Иокогама (Япония)", "Абу-Даби (ОАЭ)", "Лондон (Англия)", "Доха (Катар)");
    List<String> answers2 = Arrays.asList("Матийс де Лигт", "Криштиану Роналду", "Лионель Месси", "Вирджил ван Дейк");
    List<String> answers3 = Arrays.asList("Бразилия", "Англия", "Испания", "Аргентина");
    List<String> answers4 = Arrays.asList("Уругвай", "Франция", "Испания", "Англия");
    List<String> answers5 = Arrays.asList("Штрафной", "Мертвый мяч", "Автогол", "Ложнуй манёвр");
    List<String> answers6 = Arrays.asList("30", "24", "11", "22");
    List<String> answers7 = Arrays.asList("Бразилия", "Англия", "Испания", "Германия");
    List<String> answers8 = Arrays.asList("7,22 на 2,45 метра", "6,9 на 2,11 метра", "7,11 на 2,33 метра", "7,32 на 2,44 метра");
    List<String> answers9 = Arrays.asList("Трипл-сек", "Автогол", "Хет-трик", "Гандикап");
    List<String> answers10 = Arrays.asList("Статуетка чемпиона", "Золотой мяч", "Кубок чемпиона", "Золотая бутса");

    questions.add(new Question("1. В какой стране, и в каком городе проходил Финал Клубного чемпионата мира 2019?", answers1, "Доха (Катар)"));
    questions.add(new Question("2. Кто является лучшим бомбардиром за всю историю сборной Португалии?", answers2, "Криштиану Роналду"));
    questions.add(new Question("3. В какой стране появился и начал развиваться футбол?", answers3, "Англия"));
    questions.add(new Question("4. В какой стране впервые проходил Кубок мира ФИФА?", answers4, "Уругвай"));
    questions.add(new Question("5. Как называется гол, который футболист забил в свои ворота?", answers5, "Автогол"));
    questions.add(new Question("6. Какое количество игроков одновременно находится на игровом поле с двух сторон?", answers6, "22"));
    questions.add(new Question("7. Команда из какой страны чаще всего становилась победителем чемпионата мира?", answers7, "Бразилия"));
    questions.add(new Question("8. Назовите правильные размеры стандартных футбольных ворот?", answers8, "7,32 на 2,44 метра"));
    questions.add(new Question("9. Что оформил игрок, забивший 3 мяча за один матч?", answers9, "Хет-трик"));
    questions.add(new Question("10. Как называется самая почетная персональная награда для футболиста?", answers10, "Золотой мяч"));
}


    public List<Question> getQuestions() {
        return questions;
    }
}
