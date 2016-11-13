package org.meridor.perspective.shell.common.repository;

import org.meridor.perspective.beans.Letter;

import java.util.List;

public interface MailRepository {

    List<Letter> getLetters();

    void deleteLetter(String id);

}
