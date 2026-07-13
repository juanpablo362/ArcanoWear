package com.example.arcanomovil.data

object MockUsuariosRepository {

    private val usuarios = mutableListOf(
        Usuario("1", "Andrea Méndez", "admin@arcano.com", "admin", Rol.Administrador),
        Usuario("2", "Jorge Salinas", "jorge.salinas@arcano.com", "1234", Rol.Despachador),
        Usuario("3", "Luis Mendoza", "operador@arcano.com", "operador", Rol.Operador),
        Usuario("4", "Carla Rojas", "carla.rojas@arcano.com", "1234", Rol.Despachador),
        Usuario("5", "Diego Paredes", "diego.paredes@arcano.com", "1234", Rol.Operador),
        Usuario("6", "María Palacios", "maria.palacios@arcano.com", "1234", Rol.Despachador),
        Usuario("7", "Fernando Vega", "fernando.vega@arcano.com", "1234", Rol.Operador),
        Usuario("8", "Gabriela Cabrera", "gabriela.cabrera@arcano.com", "1234", Rol.Administrador),
        Usuario("9", "Carlos Ruiz", "despacho@arcano.com", "despacho", Rol.Despachador)
    )

    fun todos(): List<Usuario> = usuarios.toList()

    fun getById(id: String): Usuario? = usuarios.find { it.id == id }

    fun login(correo: String, password: String): Usuario? {
        return usuarios.find {
            it.correo.equals(correo.trim(), ignoreCase = true) &&
                it.password == password &&
                it.activo
        }
    }

    fun guardar(usuario: Usuario) {
        val index = usuarios.indexOfFirst { it.id == usuario.id }
        if (index >= 0) {
            usuarios[index] = usuario
        } else {
            usuarios.add(0, usuario)
        }
    }

    fun nextId(): String = ((usuarios.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1).toString()

    fun toggleActivo(id: String) {
        val index = usuarios.indexOfFirst { it.id == id }
        if (index >= 0) {
            val u = usuarios[index]
            usuarios[index] = u.copy(activo = !u.activo)
        }
    }
}
