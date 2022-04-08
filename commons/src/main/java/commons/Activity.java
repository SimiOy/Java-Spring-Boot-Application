package commons;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Activity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @JsonProperty("id")
    @Column(name = "activityID", unique = true)
    private String activityID;
    @JsonProperty("image_path")
    private String imagePath;
    private String title;
    @JsonProperty("consumption_in_wh")
    private Long energyConsumption;
    @Column(length = 1000)
    private String source;

    public Activity() {
    }

    public Activity(String activityID, String imagePath, String title, Long energyConsumption, String source) {
        this.activityID = activityID;
        this.imagePath = imagePath;
        this.title = title;
        this.energyConsumption = energyConsumption;
        this.source = source;
    }

    public void setEnergyConsumption(Long energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    public Long getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getTitle() {
        return title;
    }

    public Long getEnergyConsumption() {
        return energyConsumption;
    }

    public String getSource() {
        return source;
    }

    public String getActivityID() {
        return activityID;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}
