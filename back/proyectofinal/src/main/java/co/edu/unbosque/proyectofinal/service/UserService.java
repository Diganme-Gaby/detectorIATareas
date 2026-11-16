package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.ChangePasswordDTO;
import co.edu.unbosque.proyectofinal.dto.RegisterDTO;
import co.edu.unbosque.proyectofinal.dto.UpdateProfileDTO;
import co.edu.unbosque.proyectofinal.dto.UserDTO;
import co.edu.unbosque.proyectofinal.model.Audit.Action;
import co.edu.unbosque.proyectofinal.model.EmailToken;
import co.edu.unbosque.proyectofinal.model.EmailToken.TokenType;
import co.edu.unbosque.proyectofinal.model.User;
import co.edu.unbosque.proyectofinal.repository.EmailTokenRepository;
import co.edu.unbosque.proyectofinal.repository.UserRepository;
import co.edu.unbosque.proyectofinal.util.CifradoUtil;
import co.edu.unbosque.proyectofinal.util.EmailSender;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de usuarios: registro con verificación de correo, inicio de sesión con bloqueo por
 * intentos fallidos, recuperación de contraseña, perfil y administración de cuentas.
 *
 * <p>Implementa {@link CRUDOperation} como en la guía. Los códigos numéricos de retorno siguen el
 * mismo estilo: 0 significa éxito y cada otro valor un caso de error documentado en el método.
 *
 * <p>La contraseña se guarda con hash BCrypt y el teléfono se guarda cifrado con AES; el teléfono
 * se descifra al convertir el usuario en DTO.
 */
@Service
public class UserService implements CRUDOperation<UserDTO> {

  /** Intentos fallidos seguidos que bloquean la cuenta. */
  public static final int MAX_INTENTOS = 5;

  /** Tiempo que dura el bloqueo por intentos fallidos. */
  public static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

  /** Vigencia del enlace de verificación. */
  private static final Duration VIGENCIA_VERIFICACION = Duration.ofHours(24);

  /** Vigencia del enlace de recuperación. */
  private static final Duration VIGENCIA_RECUPERACION = Duration.ofMinutes(30);

  /** Hash BCrypt de referencia para que el tiempo de respuesta no revele si el usuario existe. */
  private static final String HASH_FALSO = new BCryptPasswordEncoder().encode("usuario-inexistente");

  /**
   * Forma de los teléfonos cifrados con la versión anterior, que usaba un IV fijo: Base64 de al
   * menos 24 caracteres. Esos valores ya no se pueden descifrar y se muestran vacíos. Un teléfono
   * en texto plano (máximo 20 caracteres) nunca cumple este formato.
   */
  private static final Pattern CIFRADO =
      Pattern.compile("^(?:[A-Za-z0-9+/]{4}){6,}(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");

  /** Estados posibles de un intento de inicio de sesión. */
  public enum LoginStatus {
    /** Credenciales correctas y cuenta habilitada. */
    OK,
    /** Usuario inexistente o contraseña incorrecta. */
    CREDENCIALES_INVALIDAS,
    /** Cuenta bloqueada por intentos fallidos. */
    BLOQUEADA,
    /** El correo aún no se ha confirmado. */
    NO_VERIFICADA,
    /** El administrador desactivó la cuenta. */
    DESACTIVADA
  }

  /** Repositorio para acceder a los datos de usuarios en la base de datos. */
  @Autowired private UserRepository userRepo;

  /** Repositorio de códigos enviados por correo. */
  @Autowired private EmailTokenRepository tokenRepo;

  /** Codificador para encriptar contraseñas de usuarios. */
  @Autowired private PasswordEncoder passwordEncoder;

  /** Envío de correos. */
  @Autowired private EmailSender emailSender;

  /** Registro de auditoría. */
  @Autowired private AuditService auditService;

  /** Llave AES de 16 caracteres para cifrar el teléfono. */
  @Value("${app.aes.key}")
  private String llaveAes;

  /** Constructor vacío. */
  public UserService() {}

  /**
   * Cuenta el número total de usuarios en la base de datos.
   *
   * @return Número total de usuarios
   */
  @Override
  public long count() {
    return userRepo.count();
  }

  /**
   * Verifica si existe un usuario con el ID especificado.
   *
   * @param id ID del usuario a verificar
   * @return true si el usuario existe, false en caso contrario
   */
  @Override
  public boolean exist(Long id) {
    return userRepo.existsById(id);
  }

  /**
   * Crea una cuenta nueva sin IP de origen. Pasa los datos a un {@link RegisterDTO} y usa el mismo
   * flujo de {@link #register(RegisterDTO, String)}.
   *
   * @param data datos del registro ya validados
   * @return 0 si se creó, 1 si el nombre de usuario ya existe, 2 si el correo ya existe
   */
  @Override
  public int create(UserDTO data) {
    RegisterDTO registro = new RegisterDTO(
        data.getUsername(), data.getEmail(), data.getNombreCompleto(), data.getTelefono(), data.getPassword());
    return register(registro, null);
  }

  /**
   * Registra una cuenta nueva, genera el código de verificación y lo envía por correo. La cuenta
   * siempre queda con rol USER; el rol solo lo cambia un administrador.
   *
   * @param data datos del registro ya validados
   * @param ip IP de origen para la auditoría
   * @return 0 si se creó, 1 si el nombre de usuario ya existe, 2 si el correo ya existe
   */
  @Transactional
  public int register(RegisterDTO data, String ip) {
    String email = data.getEmail().trim().toLowerCase();
    if (userRepo.existsByUsername(data.getUsername().trim())) {
      return 1;
    }
    if (userRepo.existsByEmail(email)) {
      return 2;
    }
    User entity =
        new User(
            data.getUsername().trim(),
            email,
            data.getNombreCompleto().trim(),
            passwordEncoder.encode(data.getPassword()));
    if (data.getTelefono() != null && !data.getTelefono().isBlank()) {
      entity.setTelefono(cifrar(data.getTelefono().trim()));
    }
    userRepo.save(entity);
    String token = crearToken(entity, TokenType.VERIFICACION, VIGENCIA_VERIFICACION);
    emailSender.enviarVerificacion(entity.getEmail(), entity.getNombreCompleto(), token);
    auditService.registrar(entity.getUsername(), Action.REGISTRO, "Se creó la cuenta " + entity.getEmail(), ip);
    return 0;
  }

  /**
   * Confirma el correo con el código del enlace.
   *
   * @param token código recibido
   * @param ip IP de origen
   * @return 0 si se confirmó, 1 si el código no existe o ya se uso, 2 si venció
   */
  @Transactional
  public int verifyEmail(String token, String ip) {
    Optional<EmailToken> found = tokenRepo.findByToken(token);
    if (found.isEmpty() || found.get().getTipo() != TokenType.VERIFICACION || found.get().isUsado()) {
      return 1;
    }
    EmailToken t = found.get();
    if (!t.esValido()) {
      return 2;
    }
    t.setUsado(true);
    tokenRepo.save(t);
    Optional<User> dueno = userRepo.findById(t.getUserId());
    if (dueno.isEmpty()) {
      return 1;
    }
    User user = dueno.get();
    user.setCorreoVerificado(true);
    userRepo.save(user);
    auditService.registrar(user.getUsername(), Action.VERIFICACION_CORREO, "Correo confirmado", ip);
    return 0;
  }

  /**
   * Reenvia el correo de verificación. Responde igual exista o no el correo, para no revelar que
   * cuentas están registradas.
   *
   * @param email correo de la cuenta
   * @param ip IP de origen
   */
  @Transactional
  public void resendVerification(String email, String ip) {
    Optional<User> found = userRepo.findByEmail(email.trim().toLowerCase());
    if (found.isPresent() && !found.get().isCorreoVerificado()) {
      User user = found.get();
      String token = crearToken(user, TokenType.VERIFICACION, VIGENCIA_VERIFICACION);
      emailSender.enviarVerificacion(user.getEmail(), user.getNombreCompleto(), token);
      auditService.registrar(user.getUsername(), Action.REENVIO_VERIFICACION, "Se reenvió la verificación", ip);
    }
  }

  /**
   * Envía el enlace de recuperación de contraseña. Responde igual exista o no el correo.
   *
   * @param email correo de la cuenta
   * @param ip IP de origen
   */
  @Transactional
  public void forgotPassword(String email, String ip) {
    Optional<User> found = userRepo.findByEmail(email.trim().toLowerCase());
    if (found.isPresent()) {
      User user = found.get();
      String token = crearToken(user, TokenType.RECUPERACION, VIGENCIA_RECUPERACION);
      emailSender.enviarRecuperacion(user.getEmail(), user.getNombreCompleto(), token);
      auditService.registrar(user.getUsername(), Action.SOLICITUD_RECUPERACION, "Se envió el enlace de recuperación", ip);
    } else {
      auditService.registrar(null, Action.SOLICITUD_RECUPERACION, "Correo no registrado", ip);
    }
  }

  /**
   * Cambia la contraseña con el código de recuperación. También quita el bloqueo por intentos.
   *
   * @param token código recibido
   * @param nuevaPassword contraseña nueva ya validada
   * @param ip IP de origen
   * @return 0 si se cambio, 1 si el código no existe o ya se uso, 2 si venció
   */
  @Transactional
  public int resetPassword(String token, String nuevaPassword, String ip) {
    Optional<EmailToken> found = tokenRepo.findByToken(token);
    if (found.isEmpty() || found.get().getTipo() != TokenType.RECUPERACION || found.get().isUsado()) {
      return 1;
    }
    EmailToken t = found.get();
    if (!t.esValido()) {
      return 2;
    }
    t.setUsado(true);
    tokenRepo.save(t);
    Optional<User> dueno = userRepo.findById(t.getUserId());
    if (dueno.isEmpty()) {
      return 1;
    }
    User user = dueno.get();
    user.setPassword(passwordEncoder.encode(nuevaPassword));
    user.setIntentosFallidos(0);
    user.setBloqueadoHasta(null);
    userRepo.save(user);
    auditService.registrar(user.getUsername(), Action.CAMBIO_CONTRASENA, "Contraseña restablecida por correo", ip);
    return 0;
  }

  /**
   * Revisa un intento de inicio de sesión antes de generar el token. Cuenta los intentos fallidos
   * y bloquea la cuenta al llegar a {@link #MAX_INTENTOS}.
   *
   * <p>El estado de la cuenta (no verificada o desactivada) solo se informa cuando la contraseña
   * es correcta.
   *
   * @param username nombre de usuario
   * @param password contraseña en texto plano
   * @param ip IP de origen
   * @return resultado del intento
   */
  @Transactional
  public LoginStatus checkLogin(String username, String password, String ip) {
    Optional<User> found = username == null ? Optional.empty() : userRepo.findByUsername(username.trim());
    if (found.isEmpty()) {
      passwordEncoder.matches(password == null ? "" : password, HASH_FALSO);
      auditService.registrar(username, Action.LOGIN_FALLIDO, "Usuario inexistente", ip);
      return LoginStatus.CREDENCIALES_INVALIDAS;
    }
    User user = found.get();
    if (!user.isAccountNonLocked()) {
      return LoginStatus.BLOQUEADA;
    }
    if (password == null || !passwordEncoder.matches(password, user.getPassword())) {
      user.setIntentosFallidos(user.getIntentosFallidos() + 1);
      if (user.getIntentosFallidos() >= MAX_INTENTOS) {
        user.setBloqueadoHasta(LocalDateTime.now().plus(DURACION_BLOQUEO));
        user.setIntentosFallidos(0);
        userRepo.save(user);
        auditService.registrar(user.getUsername(), Action.CUENTA_BLOQUEADA,
            "Cuenta bloqueada " + DURACION_BLOQUEO.toMinutes() + " minutos por intentos fallidos", ip);
        return LoginStatus.BLOQUEADA;
      }
      userRepo.save(user);
      auditService.registrar(user.getUsername(), Action.LOGIN_FALLIDO,
          "Contraseña incorrecta (intento " + user.getIntentosFallidos() + " de " + MAX_INTENTOS + ")", ip);
      return LoginStatus.CREDENCIALES_INVALIDAS;
    }
    if (!user.isCorreoVerificado()) {
      return LoginStatus.NO_VERIFICADA;
    }
    if (!user.isActivo()) {
      return LoginStatus.DESACTIVADA;
    }
    return LoginStatus.OK;
  }

  /**
   * Deja registro de un inicio de sesión correcto y reinicia los intentos fallidos.
   *
   * @param username nombre de usuario
   * @param ip IP de origen
   */
  @Transactional
  public void registerSuccessfulLogin(String username, String ip) {
    userRepo.findByUsername(username).ifPresent(user -> {
      user.setIntentosFallidos(0);
      user.setBloqueadoHasta(null);
      user.setUltimoAcceso(LocalDateTime.now());
      userRepo.save(user);
    });
    auditService.registrar(username, Action.LOGIN_EXITOSO, "Inicio de sesión", ip);
  }

  /**
   * Minutos que le faltan al bloqueo de una cuenta.
   *
   * @param username nombre de usuario
   * @return minutos restantes, mínimo 1
   */
  public long minutosBloqueo(String username) {
    return userRepo.findByUsername(username)
        .map(User::getBloqueadoHasta)
        .map(hasta -> Math.max(1, Duration.between(LocalDateTime.now(), hasta).toMinutes() + 1))
        .orElse(0L);
  }

  /**
   * Obtiene todos los usuarios de la base de datos.
   *
   * @return Lista de DTOs de usuarios, del más reciente al más antiguo
   */
  @Override
  public List<UserDTO> getAll() {
    List<UserDTO> dtoList = new ArrayList<>();
    userRepo.findAllByOrderByFechaRegistroDesc().forEach(entity -> dtoList.add(toDto(entity)));
    return dtoList;
  }

  /**
   * Los usuarios no se borran porque la auditoría y el historial los referencian. Este método hace
   * un borrado lógico: desactiva la cuenta.
   *
   * @param id ID del usuario
   * @return 0 si se desactivó, 1 si no existe
   */
  @Override
  @Transactional
  public int deleteById(Long id) {
    Optional<User> found = userRepo.findById(id);
    if (found.isEmpty()) {
      return 1;
    }
    User user = found.get();
    user.setActivo(false);
    userRepo.save(user);
    return 0;
  }

  /**
   * Actualiza nombre, teléfono y, si viene, el rol. El nombre de usuario y el correo no se pueden
   * cambiar, así que se ignoran aunque vengan en los datos.
   *
   * @param id ID del usuario
   * @param newData datos nuevos ya validados
   * @return 0 si se actualizó, 1 si no existe
   */
  @Override
  @Transactional
  public int updateById(Long id, UserDTO newData) {
    Optional<User> found = userRepo.findById(id);
    if (found.isEmpty()) {
      return 1;
    }
    User user = found.get();
    user.setNombreCompleto(newData.getNombreCompleto().trim());
    user.setTelefono(newData.getTelefono() == null || newData.getTelefono().isBlank() ? null : cifrar(newData.getTelefono().trim()));
    if (newData.getRole() != null) {
      user.setRole(newData.getRole());
    }
    userRepo.save(user);
    return 0;
  }

  /**
   * Obtiene un usuario por su ID.
   *
   * @param id ID del usuario a obtener
   * @return DTO del usuario si existe, null en caso contrario
   */
  public UserDTO getById(Long id) {
    return userRepo.findById(id).map(this::toDto).orElse(null);
  }

  /**
   * Obtiene un usuario por su nombre de usuario.
   *
   * @param username nombre de usuario
   * @return DTO del usuario si existe, null en caso contrario
   */
  public UserDTO getByUsername(String username) {
    return userRepo.findByUsername(username).map(this::toDto).orElse(null);
  }

  /**
   * Actualiza el nombre y el teléfono del propio usuario.
   *
   * @param username usuario en sesión
   * @param data datos nuevos ya validados
   * @param ip IP de origen
   * @return 0 si se actualizó, 1 si el usuario no existe
   */
  @Transactional
  public int updateProfile(String username, UpdateProfileDTO data, String ip) {
    Optional<User> found = userRepo.findByUsername(username);
    if (found.isEmpty()) {
      return 1;
    }
    User user = found.get();
    user.setNombreCompleto(data.getNombreCompleto().trim());
    user.setTelefono(data.getTelefono() == null || data.getTelefono().isBlank() ? null : cifrar(data.getTelefono().trim()));
    userRepo.save(user);
    auditService.registrar(username, Action.ACTUALIZACION_PERFIL, "Actualizó nombre y teléfono", ip);
    return 0;
  }

  /**
   * Cambia la contraseña del usuario en sesión.
   *
   * @param username usuario en sesión
   * @param data contraseña actual y nueva (la nueva ya validada)
   * @param ip IP de origen
   * @return 0 si se cambio, 1 si la contraseña actual no coincide, 2 si el usuario no existe
   */
  @Transactional
  public int changePassword(String username, ChangePasswordDTO data, String ip) {
    Optional<User> found = userRepo.findByUsername(username);
    if (found.isEmpty()) {
      return 2;
    }
    User user = found.get();
    if (data.getPasswordActual() == null || !passwordEncoder.matches(data.getPasswordActual(), user.getPassword())) {
      return 1;
    }
    user.setPassword(passwordEncoder.encode(data.getPasswordNueva()));
    userRepo.save(user);
    auditService.registrar(username, Action.CAMBIO_CONTRASENA, "Cambio de contraseña desde el perfil", ip);
    return 0;
  }

  /**
   * El administrador actualiza nombre, teléfono y rol de un usuario. El nombre de usuario y el
   * correo no se pueden cambiar.
   *
   * @param id usuario a modificar
   * @param data datos nuevos ya validados
   * @param admin administrador que hace el cambio
   * @param ip IP de origen
   * @return 0 si se actualizó, 1 si no existe, 2 si el administrador intenta quitarse su propio rol
   */
  @Transactional
  public int adminUpdate(Long id, UpdateProfileDTO data, String admin, String ip) {
    Optional<User> found = userRepo.findById(id);
    if (found.isEmpty()) {
      return 1;
    }
    User user = found.get();
    if (user.getUsername().equals(admin) && data.getRole() != null && data.getRole() != User.Role.ADMIN) {
      return 2;
    }
    UserDTO cambios = new UserDTO();
    cambios.setNombreCompleto(data.getNombreCompleto());
    cambios.setTelefono(data.getTelefono());
    cambios.setRole(data.getRole());
    updateById(id, cambios);
    auditService.registrar(admin, Action.ADMIN_ACTUALIZA_USUARIO,
        "Actualizó al usuario " + user.getUsername() + " (rol " + (data.getRole() == null ? user.getRole() : data.getRole()) + ")", ip);
    return 0;
  }

  /**
   * Activa o desactiva una cuenta. Al desactivarla, el token que tenga deja de servir porque el
   * filtro JWT revisa el estado en cada petición.
   *
   * @param id usuario a modificar
   * @param activo estado nuevo
   * @param admin administrador que hace el cambio
   * @param ip IP de origen
   * @return 0 si se cambio, 1 si no existe, 2 si el administrador intenta desactivarse a si mismo
   */
  @Transactional
  public int setActive(Long id, boolean activo, String admin, String ip) {
    Optional<User> found = userRepo.findById(id);
    if (found.isEmpty()) {
      return 1;
    }
    User user = found.get();
    if (!activo && user.getUsername().equals(admin)) {
      return 2;
    }
    user.setActivo(activo);
    userRepo.save(user);
    auditService.registrar(admin, activo ? Action.ADMIN_ACTIVA_USUARIO : Action.ADMIN_DESACTIVA_USUARIO,
        (activo ? "Activó" : "Desactivó") + " la cuenta de " + user.getUsername(), ip);
    return 0;
  }

  /**
   * Quita el bloqueo por intentos fallidos.
   *
   * @param id usuario a desbloquear
   * @param admin administrador que hace el cambio
   * @param ip IP de origen
   * @return 0 si se desbloqueó, 1 si no existe
   */
  @Transactional
  public int unlock(Long id, String admin, String ip) {
    Optional<User> found = userRepo.findById(id);
    if (found.isEmpty()) {
      return 1;
    }
    User user = found.get();
    user.setBloqueadoHasta(null);
    user.setIntentosFallidos(0);
    userRepo.save(user);
    auditService.registrar(admin, Action.ADMIN_DESBLOQUEA_USUARIO, "Desbloqueó la cuenta de " + user.getUsername(), ip);
    return 0;
  }

  /**
   * Cifra el teléfono con AES y un IV nuevo antes de guardarlo.
   *
   * @param telefono teléfono en texto plano
   * @return teléfono cifrado con la forma {@code iv:textoCifrado}
   */
  private String cifrar(String telefono) {
    return CifradoUtil.cifrar(llaveAes, telefono);
  }

  /**
   * Descifra el teléfono guardado. Los usuarios creados antes del cifrado tienen el teléfono en
   * texto plano y se devuelve tal cual; los cifrados con la versión anterior (IV fijo) se
   * devuelven vacíos.
   *
   * @param guardado teléfono como está en la base de datos
   * @return teléfono en texto plano, o null si no tiene o no se puede leer
   */
  private String descifrar(String guardado) {
    if (CifradoUtil.tieneFormato(guardado)) {
      return CifradoUtil.descifrar(llaveAes, guardado);
    }
    if (guardado == null || CIFRADO.matcher(guardado).matches()) {
      return null;
    }
    return guardado;
  }

  /**
   * Convierte la entidad en DTO. La contraseña nunca se copia.
   *
   * @param user entidad
   * @return DTO
   */
  public UserDTO toDto(User user) {
    UserDTO dto = new UserDTO();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setNombreCompleto(user.getNombreCompleto());
    dto.setTelefono(descifrar(user.getTelefono()));
    dto.setRole(user.getRole());
    dto.setActivo(user.isActivo());
    dto.setCorreoVerificado(user.isCorreoVerificado());
    dto.setBloqueado(!user.isAccountNonLocked());
    dto.setIntentosFallidos(user.getIntentosFallidos());
    dto.setFechaRegistro(user.getFechaRegistro());
    dto.setUltimoAcceso(user.getUltimoAcceso());
    return dto;
  }

  /**
   * Invalida los códigos anteriores del mismo tipo y crea uno nuevo.
   *
   * @param user dueño
   * @param tipo tipo de código
   * @param vigencia tiempo de validez
   * @return valor del código nuevo
   */
  private String crearToken(User user, TokenType tipo, Duration vigencia) {
    for (EmailToken anterior : tokenRepo.findByUserIdAndTipoAndUsadoFalse(user.getId(), tipo)) {
      anterior.setUsado(true);
      tokenRepo.save(anterior);
    }
    String valor = UUID.randomUUID().toString() + UUID.randomUUID().toString().substring(0, 8);
    tokenRepo.save(new EmailToken(valor, tipo, user.getId(), LocalDateTime.now().plus(vigencia)));
    return valor;
  }
}
