package co.edu.unbosque.proyectofinal.service;

import co.edu.unbosque.proyectofinal.dto.AuditDTO;
import co.edu.unbosque.proyectofinal.dto.GeolocationDTO;
import co.edu.unbosque.proyectofinal.dto.PublicIpDTO;
import co.edu.unbosque.proyectofinal.model.Audit;
import co.edu.unbosque.proyectofinal.model.Audit.Action;
import co.edu.unbosque.proyectofinal.repository.AuditRepository;
import co.edu.unbosque.proyectofinal.util.CifradoUtil;
import co.edu.unbosque.proyectofinal.util.NetworkUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Llena la tabla de auditoría. Cada registro guarda quién, qué, cuándo y desde dónde; la ubicación
 * se calcula con dos APIs gratuitas: una para la IP pública y otra que convierte la IP en latitud
 * y longitud. La IP se guarda cifrada con AES y se descifra al convertir el registro en DTO.
 *
 * <p>Este servicio no tiene métodos de borrado ni de actualización.
 */
@Service
public class AuditService {

  /** Logger para dejar registro en consola. */
  private static final Logger log = LoggerFactory.getLogger(AuditService.class);

  /** Máximo de IPs guardadas en memoria para no repetir consultas de ubicación. */
  private static final int MAX_CACHE = 500;

  /** Repositorio de auditoría. */
  private final AuditRepository auditRepo;

  /** Ubicaciones ya consultadas por IP. */
  private final Map<String, GeolocationDTO> cache = new ConcurrentHashMap<>();

  /** IP pública del servidor, usada cuando la petición llega desde la red local. */
  private volatile String ipPublicaServidor;

  /** Permite apagar la geolocalización (por ejemplo en las pruebas). */
  @Value("${app.geo.enabled:true}")
  private boolean geoHabilitada;

  /** API que devuelve la IP pública. */
  @Value("${app.geo.public-ip-url:https://api.ipify.org?format=json}")
  private String urlIpPublica;

  /** API que convierte una IP en ubicación; la IP se agrega al final. */
  @Value("${app.geo.locate-url:http://ip-api.com/json/}")
  private String urlUbicacion;

  /** Llave AES de 16 caracteres para cifrar la IP. */
  @Value("${app.aes.key}")
  private String llaveAes;

  /**
   * Spring inyecta aquí las dependencias.
   *
   * @param auditRepo repositorio de auditoría
   */
  public AuditService(AuditRepository auditRepo) {
    this.auditRepo = auditRepo;
  }

  /**
   * Registra una acción tomando la IP de la petición.
   *
   * @param username usuario, o null si no hay sesión
   * @param accion acción realizada
   * @param descripcion detalle legible
   * @param request petición HTTP
   */
  public void registrar(String username, Action accion, String descripcion, HttpServletRequest request) {
    registrar(username, accion, descripcion, NetworkUtil.obtenerIpCliente(request));
  }

  /**
   * Registra una acción con una IP conocida. Lo usa el procesamiento en segundo plano, que no
   * tiene petición HTTP.
   *
   * @param username usuario, o null si no hay sesión
   * @param accion acción realizada
   * @param descripcion detalle legible
   * @param ip IP de origen
   */
  public void registrar(String username, Action accion, String descripcion, String ip) {
    try {
      Audit audit = new Audit(username == null || username.isBlank() ? "anonimo" : username, accion,
          recortar(descripcion, 1000));
      audit.setIp(cifrar(ip));
      GeolocationDTO ubicacion = ubicar(ip);
      if (ubicacion != null) {
        audit.setLatitud(ubicacion.getLat());
        audit.setLongitud(ubicacion.getLon());
        audit.setCiudad(ubicacion.getCity());
        audit.setPais(ubicacion.getCountry());
      }
      auditRepo.save(audit);
    } catch (RuntimeException e) {
      log.error("No se pudo registrar la auditoría de {}: {}", accion, e.getMessage());
    }
  }

  /**
   * Consulta la auditoría con filtros opcionales.
   *
   * @param username usuario exacto, o null
   * @param accion acción, o null
   * @param desde fecha mínima, o null
   * @param hasta fecha máxima, o null
   * @return registros del más reciente al más antiguo
   */
  public List<AuditDTO> buscar(String username, Action accion, LocalDateTime desde, LocalDateTime hasta) {
    String filtroUsuario = username == null || username.isBlank() ? null : username.trim();
    List<AuditDTO> lista = new ArrayList<>();
    for (Audit a : auditRepo.findAllByOrderByFechaDesc()) {
      boolean coincide = (filtroUsuario == null || filtroUsuario.equals(a.getUsername()))
          && (accion == null || accion == a.getAccion())
          && (desde == null || !a.getFecha().isBefore(desde))
          && (hasta == null || !a.getFecha().isAfter(hasta));
      if (coincide) {
        lista.add(toDto(a));
      }
    }
    return lista;
  }

  /**
   * Devuelve el total de registros.
   *
   * @return total de registros
   */
  public long count() {
    return auditRepo.count();
  }

  /**
   * Convierte un registro en DTO y arma el enlace de Google Maps.
   *
   * @param a registro de auditoría
   * @return DTO
   */
  public AuditDTO toDto(Audit a) {
    AuditDTO dto = new AuditDTO();
    dto.setId(a.getId());
    dto.setUsername(a.getUsername());
    dto.setAccion(a.getAccion());
    dto.setDescripcion(a.getDescripcion());
    dto.setFecha(a.getFecha());
    dto.setIp(descifrar(a.getIp()));
    dto.setLatitud(a.getLatitud());
    dto.setLongitud(a.getLongitud());
    dto.setCiudad(a.getCiudad());
    dto.setPais(a.getPais());
    if (a.getLatitud() != null && a.getLongitud() != null) {
      dto.setMapaUrl("https://www.google.com/maps?q=" + a.getLatitud() + "," + a.getLongitud());
    }
    return dto;
  }

  /**
   * Busca la ubicación de una IP. Si la IP es privada (pruebas en local) se usa la IP pública del
   * servidor. Cualquier falla devuelve null y el registro se guarda sin ubicación.
   *
   * @param ip IP de origen
   * @return ubicación o null
   */
  private GeolocationDTO ubicar(String ip) {
    if (!geoHabilitada) {
      return null;
    }
    String ipConsulta = ip;
    if (NetworkUtil.esIpPrivada(ip)) {
      ipConsulta = ipPublica();
      if (ipConsulta == null) {
        return null;
      }
    }
    GeolocationDTO guardada = cache.get(ipConsulta);
    if (guardada != null) {
      return guardada;
    }
    try {
      GeolocationDTO u =
          ExternalHTTPRequestHandler.doGetAndConvertToGeolocationDto(
              urlUbicacion + ipConsulta + "?fields=status,country,city,lat,lon");
      if (u.getStatuscode() != 200 || !"success".equals(u.getStatus()) || u.getLat() == null || u.getLon() == null) {
        return null;
      }
      if (cache.size() < MAX_CACHE) {
        cache.put(ipConsulta, u);
      }
      return u;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } catch (Exception e) {
      log.warn("No se pudo ubicar la IP {}: {}", ipConsulta, e.getMessage());
      return null;
    }
  }

  /**
   * Devuelve la IP pública del servidor, consultada una sola vez.
   *
   * @return IP pública del servidor, consultada una sola vez
   */
  private String ipPublica() {
    if (ipPublicaServidor != null) {
      return ipPublicaServidor;
    }
    try {
      PublicIpDTO respuesta = ExternalHTTPRequestHandler.doGetAndConvertToPublicIpDto(urlIpPublica);
      if (respuesta.getStatuscode() == 200 && respuesta.getIp() != null) {
        ipPublicaServidor = respuesta.getIp();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.warn("No se pudo obtener la IP pública: {}", e.getMessage());
    }
    return ipPublicaServidor;
  }

  /**
   * Devuelve el texto recortado.
   *
   * @param texto texto
   * @param max longitud máxima
   * @return texto recortado
   */
  private String recortar(String texto, int max) {
    if (texto == null) {
      return null;
    }
    return texto.length() > max ? texto.substring(0, max) : texto;
  }

  /**
   * Cifra la IP con AES y un IV nuevo antes de guardarla.
   *
   * @param ip IP en texto plano
   * @return IP cifrada con la forma {@code iv:textoCifrado}, o el mismo valor si no hay IP
   */
  private String cifrar(String ip) {
    return CifradoUtil.cifrar(llaveAes, ip);
  }

  /**
   * Descifra la IP guardada. Los registros cifrados con la versión anterior (IV fijo) no se
   * pueden leer y quedan sin IP.
   *
   * @param cifrada IP como está en la base de datos, o null si el registro no tiene IP
   * @return IP en texto plano, o null
   */
  private String descifrar(String cifrada) {
    return CifradoUtil.descifrar(llaveAes, cifrada);
  }
}
