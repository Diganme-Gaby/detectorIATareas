import { Component, inject, OnInit, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { UpdateProfileModel, UserModel } from '../../models/user.model';
import { Adminservice } from '../../services/adminservice';
import { Authservice } from '../../services/authservice';
import { Formatoutil } from '../../util/formatoutil';
import { Pdfutil } from '../../util/pdfutil';
import { Toastrutil } from '../../util/toastrutil';

/**
 * Administración de usuarios.
 *
 * Lista todas las cuentas con su estado, permite buscarlas, editar el nombre, el teléfono y
 * el rol (el usuario y el correo no se pueden cambiar), activarlas, desactivarlas y
 * desbloquearlas, y generar el reporte PDF de usuarios.
 */
@Component({
  selector: 'app-adminusuarioscomponent',
  standalone: false,
  styleUrl: './adminusuarioscomponent.css',
  templateUrl: './adminusuarioscomponent.html',
})
export class Adminusuarioscomponent implements OnInit {
  /** Administración. */
  private adminService: Adminservice = inject(Adminservice);
  /** Mensajes tipo toast. */
  private toastr: Toastrutil = inject(Toastrutil);
  /** Visualización y descarga de PDF. */
  private pdf: Pdfutil = inject(Pdfutil);
  /** Sesión actual; el administrador no puede desactivarse a sí mismo. */
  auth: Authservice = inject(Authservice);
  /** Formato de fechas para la plantilla. */
  formato: Formatoutil = inject(Formatoutil);

  /** Todas las cuentas. */
  usuarios: WritableSignal<UserModel[]> = signal([]);
  /** Cantidad de cuentas que reporta el back. */
  total: WritableSignal<number> = signal(0);
  /** Indica si la consulta está en curso. */
  cargando: WritableSignal<boolean> = signal(true);
  /** Página actual de la tabla, empezando en 1. */
  pagina: WritableSignal<number> = signal(1);
  /** Filas por página. */
  porPagina: WritableSignal<number> = signal(10);

  /** Texto del filtro rápido. */
  busqueda: string = '';
  /** Id para buscar una cuenta puntual. */
  idBuscar: number | null = null;

  /** Cuenta que se está editando, o `null`. */
  editando: WritableSignal<UserModel | null> = signal(null);
  /** Formulario de edición. */
  datos: UpdateProfileModel = { nombreCompleto: '', telefono: '', role: 'USER' };
  /** Indica si se están guardando los cambios. */
  guardando: WritableSignal<boolean> = signal(false);

  /** Carga las cuentas al entrar. */
  ngOnInit(): void {
    this.cargar();
  }

  /** Pide las cuentas y el total al back. */
  cargar(): void {
    this.adminService.usuarios().subscribe({
      next: (respuesta: HttpResponse<UserModel[]>) => {
        this.usuarios.set(respuesta.body ?? []);
        this.cargando.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.cargando.set(false);
        this.toastr.error(this.auth.mensajeError(error, 'No se pudieron cargar los usuarios.'));
      },
    });
    this.adminService.contarUsuarios().subscribe({
      next: (total: number) => this.total.set(total),
      error: () => this.total.set(this.usuarios().length),
    });
  }

  /**
   * Filtro rápido en pantalla por usuario, nombre o correo.
   *
   * @returns cuentas que coinciden con la búsqueda
   */
  filtrados(): UserModel[] {
    const texto: string = this.busqueda.trim().toLowerCase();
    if (texto === '') {
      return this.usuarios();
    }
    return this.usuarios().filter(
      (u: UserModel) =>
        u.username.toLowerCase().includes(texto) ||
        u.nombreCompleto.toLowerCase().includes(texto) ||
        u.email.toLowerCase().includes(texto),
    );
  }

  /**
   * @returns filas de la página actual
   */
  paginados(): UserModel[] {
    const inicio: number = (this.pagina() - 1) * this.porPagina();
    return this.filtrados().slice(inicio, inicio + this.porPagina());
  }

  /**
   * Cambia la cantidad de filas por página y vuelve a la primera.
   *
   * @param cantidad filas por página
   */
  cambiarPorPagina(cantidad: number): void {
    this.porPagina.set(cantidad);
    this.pagina.set(1);
  }

  /** Busca una cuenta por id y la abre para editar. */
  buscarPorId(): void {
    if (this.idBuscar === null) {
      return;
    }
    this.adminService.usuarioPorId(this.idBuscar).subscribe({
      next: (usuario: UserModel) => this.abrirEdicion(usuario),
      error: (error: HttpErrorResponse) => {
        if (error.status === 404) {
          this.toastr.advertencia('No existe un usuario con el ID ' + this.idBuscar + '.');
        } else {
          this.toastr.error(this.auth.mensajeError(error, 'No se pudo buscar el usuario.'));
        }
      },
    });
  }

  /**
   * Abre el formulario de edición con los datos de la cuenta.
   *
   * @param usuario cuenta a editar
   */
  abrirEdicion(usuario: UserModel): void {
    this.editando.set(usuario);
    this.datos = {
      nombreCompleto: usuario.nombreCompleto,
      telefono: usuario.telefono ?? '',
      role: usuario.role,
    };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /** Cierra el formulario de edición. */
  cerrarEdicion(): void {
    this.editando.set(null);
  }

  /** Guarda los cambios de la cuenta. */
  guardar(): void {
    const usuario: UserModel | null = this.editando();
    if (usuario === null) {
      return;
    }
    this.guardando.set(true);
    this.adminService.actualizarUsuario(usuario.id, this.datos).subscribe({
      next: (respuesta: string) => {
        this.guardando.set(false);
        this.toastr.exito(respuesta);
        this.editando.set(null);
        this.cargar();
      },
      error: (error: HttpErrorResponse) => {
        this.guardando.set(false);
        this.toastr.error(this.auth.mensajeError(error, 'No se pudo actualizar el usuario.'));
      },
    });
  }

  /**
   * Activa la cuenta si está inactiva o la desactiva si está activa.
   *
   * @param usuario cuenta
   */
  cambiarEstado(usuario: UserModel): void {
    const accion = usuario.activo
      ? this.adminService.desactivar(usuario.id)
      : this.adminService.activar(usuario.id);
    accion.subscribe(this.alResponder('No se pudo cambiar el estado de la cuenta.'));
  }

  /**
   * Quita el bloqueo por intentos fallidos.
   *
   * @param usuario cuenta
   */
  desbloquear(usuario: UserModel): void {
    this.adminService.desbloquear(usuario.id).subscribe(this.alResponder('No se pudo desbloquear.'));
  }

  /**
   * Manejo común de las acciones sobre una cuenta: muestra el mensaje y recarga la lista.
   *
   * @param porDefecto mensaje de error si el back no envía uno
   * @returns observador para `subscribe`
   */
  private alResponder(porDefecto: string) {
    return {
      next: (respuesta: string) => {
        this.toastr.exito(respuesta);
        this.cargar();
      },
      error: (error: HttpErrorResponse) =>
        this.toastr.error(this.auth.mensajeError(error, porDefecto)),
    };
  }

  /** Abre el reporte de usuarios en una pestaña nueva. */
  verPdf(): void {
    const pestana: Window | null = this.pdf.abrirPestana();
    this.adminService.reporteUsuarios(false).subscribe({
      next: (archivo: Blob) => this.pdf.mostrar(archivo, pestana),
      error: () => {
        pestana?.close();
        this.toastr.error('No se pudo generar el reporte.');
      },
    });
  }

  /** Descarga el reporte de usuarios. */
  descargarPdf(): void {
    this.adminService.reporteUsuarios(true).subscribe({
      next: (archivo: Blob) => this.pdf.descargar(archivo, 'reporte-usuarios.pdf'),
      error: () => this.toastr.error('No se pudo descargar el reporte.'),
    });
  }
}
