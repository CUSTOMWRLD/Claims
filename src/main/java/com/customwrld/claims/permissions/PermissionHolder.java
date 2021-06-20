package com.customwrld.claims.permissions;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class PermissionHolder {

    @Getter
    private Map<PlotPermission, PlotPermissionLevel> permissions;

    public PermissionHolder() {
        permissions = new HashMap<>();
        setDefaultPermissions();
    }

    public PermissionHolder(Object obj) {
        this();

        if (!(obj instanceof Map))
            return;

        Map<?, ?> simplePermissions = (Map<?, ?>) obj;

        for (Map.Entry<?, ?> ent : simplePermissions.entrySet()) {
            if (!(ent.getKey() instanceof String) || !(ent.getValue() instanceof Integer))
                continue;

            PlotPermission permission = PlotPermission.byId((String) ent.getKey());

            if (permission == null)
                continue;

            PlotPermissionLevel level = ((Integer) ent.getValue() >= 0) ? PlotPermissionLevel.values()[(Integer) ent.getValue()] : permission.getDefaultLevel();
            setPermissionLevel(permission, level);
        }
    }

    public Map<String, Integer> getSimplePermissions() {
        Map<String, Integer> simplePermissions = new HashMap<>();
        for (Map.Entry<PlotPermission, PlotPermissionLevel> ent : permissions.entrySet()) {
            simplePermissions.put(String.valueOf(ent.getKey().getId()), ent.getValue().ordinal());
        }
        return simplePermissions;
    }

    public void setPermissionLevel(PlotPermission permission, PlotPermissionLevel level) {
        permissions.put(permission, level);
    }

    public PlotPermissionLevel getPermissionLevel(PlotPermission permission) {
        return permissions.get(permission);
    }

    public void setDefaultPermission(PlotPermission permission) {
        permissions.put(permission, permission.getDefaultLevel());
    }

    public void setDefaultPermissions() {
        for (PlotPermission permission : PlotPermission.values()) {
            setDefaultPermission(permission);
        }
    }

}