package com.hostel.listener;

import com.hostel.dao.impl.UserDAOImpl;
import com.hostel.model.Warden;
import com.hostel.model.enums.UserStatus;
import com.hostel.util.DBConnection;
import com.hostel.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@WebListener
public final class AppStartupListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(AppStartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            String uploadDirectory = DBConnection.getAppProperty("upload.dir")
                    .replace("${user.home}", System.getProperty("user.home"));
            Files.createDirectories(Path.of(uploadDirectory));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create the configured upload directory.", exception);
        }

        UserDAOImpl userDAO = new UserDAOImpl();
        if (!userDAO.existsWardenAccount()) {
            Warden warden = new Warden(0, DBConnection.getAppProperty("warden.name"),
                    DBConnection.getAppProperty("warden.email"), "0000000000",
                    PasswordUtil.hash(DBConnection.getAppProperty("warden.password")),
                    UserStatus.ACTIVE, LocalDateTime.now());
            userDAO.insert(warden);
            LOGGER.warning("Default warden account created. Change its configured password before deployment.");
        }
    }
}
