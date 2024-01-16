package com.project.telegrambot.service;

import com.project.telegrambot.config.BotConfig;
import com.project.telegrambot.model.User;
import com.project.telegrambot.model.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.project.telegrambot.model.User.*;


@Slf4j
@Component
public class NewsFromSiteBot extends TelegramLongPollingBot {


    @Autowired
    private UserRepository userRepository;

    final BotConfig config;

    static final String HELP_TEXT = "Here should be help for using this bot.";

    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";

    public NewsFromSiteBot(BotConfig config) {

        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "register and get a welcome message"));
        listOfCommands.add(new BotCommand("/mydata", "get your data stored"));
        listOfCommands.add(new BotCommand("/help", "info how to use this bot"));
         //listOfCommands.add(new BotCommand("/stop", "stop sending new rss to you"));
        try{
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));

        }
        catch (TelegramApiException e) {
            System.out.println(e.getMessage()); // TODO log
        }
    }



    @Override
    public String getBotUsername() {

        return config.getBotName();
    }

    @Override
    public String getBotToken() {

        return config.getToken();
    }


    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":

                        registerUser(update.getMessage());
                        startCommandReceived(chatId, update.getMessage().getChat().getUserName());
                        break;

                case "/help":

                        sendMessage(chatId, HELP_TEXT);
                        break;

                case "/mydata":

                 //       myDataCommand(chatId, update.getMessage().getChat().getUserName()); //TODO solve data type problem
                        break;



                case "/stop":

                    sendMessage(chatId, "there should be stopping using this bot and receiving news");
                        break;


                case "/register":
                    
                    register(chatId);
                    break;
                    
                default:
                    sendMessage(chatId, "Sorry, command was not recognized.");

            }


            }
        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals(YES_BUTTON)){
                String text = "You pressed YES button.";
                executeMessageText(chatId, text, messageId);


            } else if (callbackData.equals(NO_BUTTON)) {
                String text = "You pressed NO button.";
                executeMessageText(chatId, text, messageId);
            }
        }



    }

    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var yesButton = new InlineKeyboardButton();

        yesButton.setText("Yes");
        yesButton.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();

        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void registerUser(Message msg) {
        if(userRepository.findById(msg.getChatId()).isEmpty()){

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
        }
    }

    private void startCommandReceived(long chatId, String name){

        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you!" + " :blush:");
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);
    }

    private void myDataCommand(long chatId, Message msg) {

      //  User chatId = User.getChatId();
        if(userRepository.findById(msg.getChatId()).isEmpty()){
            String  answer = "User is undefined. For registration choose 'start' command, please.";
            sendMessage(chatId, answer);
        }
        else {
            Optional<User> findChatId = userRepository.findById(chatId);
            sendMessage(chatId, String.valueOf(msg.getChatId()) );  //TODO user data schould be here

        }
    }
//TODO create method user data from msg.methods and chat.get... methods
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(); // TODO make this part as a new method
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add("weather");
        row.add("get random joke");

        keyboardRows.add(row);

        row = new KeyboardRow();

        row.add("register");
        row.add("check my data");
        row.add("delete my data");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);


        try {
            execute(message);
        }
        catch (TelegramApiException e) {

        }
    }

    private void executeMessageText(long chatId,String text, long messageId){
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);
        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}

