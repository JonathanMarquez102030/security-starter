package com.jonathanmarquezperez.security.security.enums;

/**
 * Tipo de regla de autorización aplicable a un conjunto de rutas.
 */
public enum RuleType {
    /** Acceso libre sin autenticación. */
    PERMIT_ALL,

    /** Requiere estar autenticado, sin importar el rol. */
    AUTHENTICATED,

    /** Requiere exactamente un rol. Usa roles[0]. */
    HAS_ROLE,

    /** Requiere al menos uno de los roles listados. */
    HAS_ANY_ROLE,

    /** Requiere TODOS los roles listados simultáneamente. */
    HAS_ALL_ROLES,

    /** Requiere exactamente una authority. Usa authorities[0]. */
    HAS_AUTHORITY,

    /** Requiere al menos una de las authorities listadas. */
    HAS_ANY_AUTHORITY,

    /** Requiere TODAS las authorities listadas simultáneamente. */
    HAS_ALL_AUTHORITIES,

    /** Bloquea el acceso siempre. */
    DENY_ALL
}