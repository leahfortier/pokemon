package trainer.player.pokedex;

import util.serialization.Serializable;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;

class PokedexInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<String> locations;
    private PokedexStatus status;

    PokedexInfo() {
        locations = new ArrayList<>();
        status = PokedexStatus.NOT_SEEN;
    }

    void addLocation(String location) {
        if (StringUtils.isNullOrEmpty(location) || locations.contains(location)) {
            return;
        }

        locations.add(location);
    }

    List<String> getLocations() {
        if (!locations.isEmpty()) {
            return locations;
        }

        List<String> list = new ArrayList<>();
        list.add("Area Unknown");

        return list;
    }

    void setStatus(PokedexStatus status) {
        if (this.status.getWeight() < status.getWeight()) {
            this.status = status;
        }
    }

    boolean isStatus(PokedexStatus status) {
        return this.status == status;
    }
}
