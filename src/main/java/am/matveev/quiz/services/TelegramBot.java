package am.matveev.quiz.services;

import am.matveev.quiz.config.BotConfig;
import am.matveev.quiz.model.Question;
import am.matveev.quiz.model.Quiz;
import am.matveev.quiz.model.Quiz2;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final Quiz quiz;
    private final Quiz2 quiz2;
    private final UserService userService;
    private boolean greetingSent = false;
    private int currentQuestionIndex = 0;
    private boolean isFirstQuizCompleted = false;
    private int maxFirstQuizScore;
    private boolean continueButtonShown = false;
    private boolean allSecondQuizAnswersCorrect = false;

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessageUpdate(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQueryUpdate(update);
        }
    }

    private void handleMessageUpdate(Update update) throws TelegramApiException{
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        switch (messageText) {
            case "/start":
                handleStartCommand(chatId);
                break;
            case "/continue":
                handleContinueCommand(chatId);
                break;
            case "/repeat":
                handleRepeatCommand(chatId);
                break;
            case "/again":
                handleAgainCommand(chatId);
                break;
            default:
                handleUserAnswer(chatId, messageText);
                break;
        }
    }

    private void handleCallbackQueryUpdate(Update update) throws TelegramApiException{
        String userAnswer = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (userAnswer.equals("continue")) {
            handleContinueCommand(chatId);
        } else {
            checkAnswer(chatId, userAnswer);
        }
    }

    private void handleStartCommand(long chatId) {
        resetQuiz();
        sendRegistrationMessage(chatId);
    }

    private void handleContinueCommand(long chatId) throws TelegramApiException{
        if (isFirstQuizCompleted) {
            startSecondQuiz(chatId);
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Вы не можете продолжить викторину, пока не завершите первую. Продолжить первую викторину, отправьте ответ на текущий вопрос или дождитесь завершения.");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Failed to send message", e);
            }
        }
    }

    private void handleRepeatCommand(long chatId) {
        startFirstQuiz(chatId);
    }

    private void handleAgainCommand(long chatId) throws TelegramApiException{
        startSecondQuiz(chatId);
    }

    private void handleUserAnswer(long chatId, String userAnswer) throws TelegramApiException{
        userService.registerUser(chatId, userAnswer);
        sendNextQuestion(chatId);
    }

    private void sendRegistrationMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Для прохождения викторины, пожалуйста, зарегистрируйтесь. Введите ваше имя:");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send registration message", e);
        }
    }

    private void sendGreeting(long chatId) throws TelegramApiException {
        InputStream inputStream = getClass().getResourceAsStream("/static/1200x630wa.png");
        InputFile inputFile = new InputFile(inputStream, "1200x630wa.png");

        SendPhoto photo = new SendPhoto();
        photo.setChatId(String.valueOf(chatId));
        photo.setPhoto(inputFile);
        execute(photo);

        String greetingMessage = "Привет! Это Quiz Bot. Попробуй пройти нашу футбольную викторину.";
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(greetingMessage);
        execute(message);
    }

    private void sendNextQuestion(long chatId) throws TelegramApiException {
        if (!greetingSent) {
            sendGreeting(chatId);
            greetingSent = true;
        }

        List<Question> questions;
        if (!isFirstQuizCompleted) {
            questions = quiz.getQuestions();
        } else {
            questions = quiz2.getQuestions();
        }

        Question question = questions.get(currentQuestionIndex);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(question.getQuestionText());

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (String answer : question.getAnswers()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(answer);
            button.setCallbackData(answer);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
        }

        if (currentQuestionIndex == questions.size() - 1 && continueButtonShown) {
            InlineKeyboardButton continueButton = new InlineKeyboardButton();
            continueButton.setText("Продолжить");
            continueButton.setCallbackData("continue");

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(continueButton);
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        execute(message);
    }


    private void startSecondQuiz(long chatId) throws TelegramApiException {
        resetQuiz();
        sendNextQuestion(chatId);
    }

    private void startFirstQuiz(long chatId) {
        isFirstQuizCompleted = false;
        maxFirstQuizScore = 0;
        currentQuestionIndex = 0;
        continueButtonShown = false;

        try {
            sendNextQuestion(chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to start first quiz", e);
        }
    }

    private int score = 0;

    private void checkAnswer(long chatId, String userAnswer) throws TelegramApiException {
        List<Question> questions;
        if (!isFirstQuizCompleted) {
            questions = quiz.getQuestions();
        } else {
            questions = quiz2.getQuestions();
        }

        Question currentQuestion = questions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getCorrectAnswer();
        String response;
        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            response = "Верно!";
            score++;
            if (!isFirstQuizCompleted) {
                maxFirstQuizScore++;
            }
        } else {
            response = "Неверно. Правильный ответ: " + correctAnswer;
        }
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(response);
        execute(message);

        currentQuestionIndex++;

        if (currentQuestionIndex < questions.size()) {
            sendNextQuestion(chatId);
        } else {
            if (!isFirstQuizCompleted) {
                isFirstQuizCompleted = true;
                currentQuestionIndex = 0;
                if (maxFirstQuizScore >= 10) {
                    SendMessage continueMessage = new SendMessage();
                    continueMessage.setChatId(String.valueOf(chatId));
                    continueMessage.setText("Для доступа ко второй викторине, введите /continue.");
                    execute(continueMessage);
                } else {
                    SendMessage repeatMessage = new SendMessage();
                    repeatMessage.setChatId(String.valueOf(chatId));
                    repeatMessage.setText("Поздравляем! Вы завершили первую викторину. Но для доступа ко второй викторине, наберите минимальное количество баллов (не менее 10): " + quiz.getQuestions().size() + ". Чтобы начать заново, отправьте команду /repeat.");
                    execute(repeatMessage);
                }
            } else {
                if (score < 10) {
                    SendMessage repeatMessage = new SendMessage();
                    repeatMessage.setChatId(String.valueOf(chatId));
                    repeatMessage.setText("Вы набрали менее 10 баллов во второй викторине. Для повторного прохождения введите /again.");
                    execute(repeatMessage);
                } else {
                    SendMessage endMessage = new SendMessage();
                    endMessage.setChatId(String.valueOf(chatId));
                    endMessage.setText("Вопросы закончились. Спасибо за участие! Вы набрали : " + score + " балла(ов). Чтобы начать заново, отправьте команду /start");
                    execute(endMessage);
                    resetQuiz();
                }
            }
        }
    }

    private void resetQuiz() {
        score = 0;
        currentQuestionIndex = 0;
        continueButtonShown = false;
    }
}
