package com.example.libraryserver.user.service;

import org.passay.CharacterCharacteristicsRule;
import org.passay.CharacterRule;
import org.passay.DictionarySubstringRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RepeatCharacterRegexRule;
import org.passay.RuleResult;
import org.passay.UsernameRule;
import org.passay.WhitespaceRule;
import org.passay.dictionary.ArrayWordList;
import org.passay.dictionary.WordList;
import org.passay.dictionary.WordListDictionary;
import org.passay.dictionary.WordLists;
import org.passay.dictionary.sort.ArraysSort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Password policy validator.
 * Uses recommendations from https://pages.nist.gov/800-63-3/sp800-63b.html (section 5.1.1.2 Memorized Secret Verifiers)
 */
public class PasswordValidationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PasswordValidationService.class);

  /* https://github.com/danielmiessler/SecLists/blob/master/Passwords/darkweb2017-top100.txt */
  private static final String PASSWORD_LIST_TXT = "password-list.txt";

  private PasswordValidator passwordValidator;

  @PostConstruct
  public void init() {

    WordList wordList;
    try {
      ClassPathResource resource = new ClassPathResource(PASSWORD_LIST_TXT);
      wordList = WordLists.createFromReader(
              new FileReader[] {new FileReader(resource.getFile())},
              false,
              new ArraysSort());
      LOGGER.info("Successfully loaded the password list from {} with size {}", resource.getURL(), wordList.size());
    } catch (IOException ex) {
      wordList = new ArrayWordList(
              new String[] {"password", "Password", "123456", "12345678", "admin", "geheim", "secret"},
              false, new ArraysSort());
      LOGGER.warn("Error loading the password list: {}", ex.getMessage());
    }

    CharacterCharacteristicsRule characteristicsRule = new CharacterCharacteristicsRule();

    characteristicsRule.setNumberOfCharacteristics(3);

    characteristicsRule.getRules().add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
    characteristicsRule.getRules().add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
    characteristicsRule.getRules().add(new CharacterRule(EnglishCharacterData.Digit, 1));
    characteristicsRule.getRules().add(new CharacterRule(EnglishCharacterData.Special, 1));

    this.passwordValidator= new PasswordValidator(Arrays.asList(
            new LengthRule(12, 64),
            characteristicsRule,
            new RepeatCharacterRegexRule(4),
            new UsernameRule(),
            new WhitespaceRule(),
            new DictionarySubstringRule(new WordListDictionary(wordList))));
  }

  public void validate(String username, String password) {
    RuleResult result = this.passwordValidator.validate(new PasswordData(username, password));
    if (!result.isValid()) {
      List<String> messages = passwordValidator.getMessages(result);
      LOGGER.warn("Password validation failed");
      messages.forEach(LOGGER::info);
      throw new InvalidPasswordError(messages);
    } else {
      LOGGER.info("Password validated successfully");
    }
  }
}
