package com.bezkoder.springjwt.Service;

import com.bezkoder.springjwt.exceptions.UserNotFoundException;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public class UserService {


    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public List<Object[]> countUsersByRole() {
        return userRepository.countUsersByRole();
    }


    @Scheduled(cron = "0 44 22 * * *") // Planifie l'envoi du sondage tous les jours à 22h37
    public void sendSurvey() {
        System.out.println("Envoi du sondage aux utilisateurs...");

        String surveyId = generateSurveyId();

        String[] questions = {
                "Comment évaluez-vous notre service client ?",
                "Quelles fonctionnalités aimeriez-vous voir ajoutées à notre plateforme ?",
                "Avez-vous des suggestions pour améliorer l'expérience utilisateur ?",
                "before fix user"
        };

        String[][] options = {
                {"Excellent", "Bon", "Moyen", "Mauvais"},
                {"Chat en direct", "Notifications en temps réel", "Meilleure intégration avec les réseaux sociaux", "Autre"},
                {"Interface utilisateur plus intuitive", "Réduction des temps de chargement", "Personnalisation des paramètres", "Autre"}
        };


    }

    private String generateSurveyId() {
        return "SURVEY-" + System.currentTimeMillis();
    }


    public User getUserByUserName(String username) throws UserNotFoundException {
        Optional<User> user1=userRepository.findByUsername(username);

        if (user1.isPresent()){
            return user1.get();
        }else {
            throw new UserNotFoundException();
        }
    }
}

