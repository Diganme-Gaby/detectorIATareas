package co.edu.unbosque.proyectofinal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** Pruebas de la obtención de la IP del cliente. */
class NetworkUtilTest {

  /** Constructor vacío de la clase de pruebas. */
  NetworkUtilTest() {}

  /** Detrás de un proxy se toma la primera IP de X-Forwarded-For. */
  @Test
  void usaXForwardedFor() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("X-Forwarded-For", "181.49.10.5, 10.0.0.2");
    req.setRemoteAddr("10.0.0.2");
    assertEquals("181.49.10.5", NetworkUtil.obtenerIpCliente(req));
  }

  /** Un encabezado con caracteres raros no se guarda tal cual. */
  @Test
  void limpiaEncabezadosManipulados() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("X-Forwarded-For", "<script>alert(1)</script>");
    String ip = NetworkUtil.obtenerIpCliente(req);
    assertFalse(ip.contains("<"));
  }

  /** IPs privadas y públicas. */
  @Test
  void detectaIpsPrivadas() {
    assertTrue(NetworkUtil.esIpPrivada("127.0.0.1"));
    assertTrue(NetworkUtil.esIpPrivada("192.168.1.20"));
    assertTrue(NetworkUtil.esIpPrivada("172.20.0.4"));
    assertTrue(NetworkUtil.esIpPrivada("0:0:0:0:0:0:0:1"));
    assertFalse(NetworkUtil.esIpPrivada("172.40.0.4"));
    assertFalse(NetworkUtil.esIpPrivada("181.49.10.5"));
  }
}
