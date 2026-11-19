package co.edu.unbosque.proyectofinal.configuration;

import co.edu.unbosque.proyectofinal.model.User;
import co.edu.unbosque.proyectofinal.repository.UserRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase de configuración para cargar datos iniciales en la base de datos. Crea usuarios
 * predeterminados (administrador y usuario normal) al iniciar la aplicación si estos no existen
 * previamente. Ambos quedan con el correo verificado para poder entrar de una vez.
 */
@Configuration
public class LoadDatabase {
  /** Logger para registrar mensajes durante la carga de datos. */
  private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

  /** Usuario del administrador inicial. */
  @Value("${app.admin.username}")
  private String adminUsername;

  /** Correo del administrador inicial. */
  @Value("${app.admin.email}")
  private String adminEmail;

  /** Contraseña del administrador inicial. */
  @Value("${app.admin.password}")
  private String adminPassword;

  /** Constructor vacío. */
  public LoadDatabase() {}

  /**
   * Inicializa la base de datos con usuarios predeterminados. Crea un usuario administrador y un
   * usuario normal si no existen.
   *
   * @param userRepo Repositorio de usuarios para acceder a la base de datos
   * @param passwordEncoder Codificador de contraseñas para encriptar las contraseñas de los
   *     usuarios
   * @return Un CommandLineRunner que se ejecuta al iniciar la aplicación
   */
  @Bean
  CommandLineRunner initDatabase(UserRepository userRepo, PasswordEncoder passwordEncoder) {

    return args -> {
      Optional<User> found = userRepo.findByUsername(adminUsername);
      if (found.isPresent()) {
        log.info("El administrador ya existe, omitiendo la creación del administrador...");
      } else {
        User adminUser =
            new User(
                adminUsername,
                adminEmail.toLowerCase(),
                "Administrador del sistema",
                passwordEncoder.encode(adminPassword),
                User.Role.ADMIN);
        adminUser.setCorreoVerificado(true);
        userRepo.save(adminUser);
        log.info("Precargando usuario administrador");
      }
      Optional<User> found2 = userRepo.findByUsername("normaluser");
      if (found2.isPresent()) {
        log.info("El usuario normal ya existe, omitiendo la creación del usuario normal...");
      } else {
        User normalUser =
            new User(
                "normaluser",
                "normaluser@detectoria.local",
                "Usuario de pruebas",
                passwordEncoder.encode(adminPassword),
                User.Role.USER);
        normalUser.setCorreoVerificado(true);
        userRepo.save(normalUser);
        log.info("Precargando usuario normal");
      }
    };
  }
}
