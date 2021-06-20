package com.customwrld.claims.options;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class OptionsHolder {

    @Getter
    private Map<PlotOption, Boolean> options;

    public OptionsHolder() {
        options = new HashMap<>();
        resetOptions();
    }

    public OptionsHolder(Object obj) {
        this();

        if (!(obj instanceof Map)) { return; }

        Map<?, ?> simpleOptions = (Map<?, ?>) obj;
        for (Map.Entry<?, ?> ent : simpleOptions.entrySet()) {
            if (!(ent.getKey() instanceof String) || !(ent.getValue() instanceof Boolean)) { continue; }

            PlotOption option = PlotOption.byId((String) ent.getKey());

            if (option == null) { continue; }

            setOption(option, (Boolean) ent.getValue());
        }
    }

    public Map<String, Boolean> getSimpleOptions() {
        Map<String, Boolean> simplePermissions = new HashMap<>();
        for (Map.Entry<PlotOption, Boolean> ent : options.entrySet()) {
            simplePermissions.put(String.valueOf(ent.getKey().getId()), ent.getValue());
        }
        return simplePermissions;
    }

    public void setOption(PlotOption option, boolean state) {
        options.put(option, state);
    }

    public boolean getOptionState(PlotOption option) {
        return options.get(option);
    }

    public void resetOption(PlotOption option) {
        options.put(option, option.getDefaultValue());
    }

    public void resetOptions() {
        for (PlotOption permission : PlotOption.values()) {
            resetOption(permission);
        }
    }

}
