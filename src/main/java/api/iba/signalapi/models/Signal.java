package api.iba.signalapi.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Signal {
    @Id
    private String nodeId;
    private Integer samplingIntervalMs;
    private Integer deadbandValue;
    private String deadbandType;
    private Boolean active;
}
