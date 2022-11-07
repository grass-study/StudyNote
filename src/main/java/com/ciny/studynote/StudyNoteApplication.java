package com.ciny.studynote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class StudyNoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyNoteApplication.class, args);
    }

}
