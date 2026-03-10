package com.kmaleon.security;

public final class Roles {
    public static final String SUPER_ADMIN    = "ROLE_SUPER_ADMIN";
    public static final String ADMIN          = "ROLE_ADMIN";
    public static final String INVENTORY_ADMIN = "ROLE_INVENTORY_ADMIN";
    public static final String ALMACENERO     = "ROLE_ALMACENERO";
    public static final String ENCARGADO      = "ROLE_ENCARGADO_SUCURSAL";

    // SpEL expressions para @PreAuthorize
    public static final String ADMIN_OR_SUPER      = "hasAnyRole('SUPER_ADMIN','ADMIN')";
    public static final String USERS_MANAGERS      = "hasAnyRole('SUPER_ADMIN','INVENTORY_ADMIN')";
    public static final String INVENTORY_STAFF     = "hasAnyRole('SUPER_ADMIN','INVENTORY_ADMIN','ALMACENERO','ENCARGADO_SUCURSAL')";
    public static final String INVENTORY_MANAGERS  = "hasAnyRole('SUPER_ADMIN','INVENTORY_ADMIN')";
    public static final String ALMACENERO_OR_ADMIN = "hasAnyRole('SUPER_ADMIN','INVENTORY_ADMIN','ALMACENERO')";
    public static final String ENCARGADO_OR_ADMIN  = "hasAnyRole('SUPER_ADMIN','INVENTORY_ADMIN','ENCARGADO_SUCURSAL')";

    private Roles() {}
}
