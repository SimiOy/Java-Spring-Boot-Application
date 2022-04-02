package commons;

import constants.QuestionTypes;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
@Table(name = "Question")
public class Question {

    @Id
    @Column(name = "id")
    public Long id;

    @Column(name = "type")
    public QuestionTypes type;

    @Column(name = "text")
    public String text;

    @Column(name = "pointer")
    public Long pointer;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Column(name = "foundActivities")
    public Set<LongWrapper> foundActivities;

    @Column(name = "lastLobbyToken")
    public String lastLobbyToken;

    public Question() {
        // for object mapper
    }

    public Question(Long id, QuestionTypes type, Long pointer, Set<Long> foundActivities, String lastLobbyToken) {
        this.id = id;
        this.type = type;
        this.lastLobbyToken = lastLobbyToken;
        switch (type){
            case MULTIPLE_CHOICE_QUESTION: this.text = "What requires more energy?";
                break;
            case ESTIMATION_QUESTION: this.text = "How much energy does it take?";
                break;
            case ENERGY_ALTERNATIVE_QUESTION: this.text = "Instead of ";
                break;
            case GUESS_X: this.text = "How much energy does this consume?";
                break;
            default: this.text = "This question has no type.";
        }
        this.pointer = pointer;
        this.foundActivities = new LinkedHashSet<>();
        for(Long l : foundActivities)
        {
            this.foundActivities.add(new LongWrapper(l));
        }
    }

    public Long getId() {
        return id;
    }

    public QuestionTypes getType() {
        return type;
    }

    public void setType(QuestionTypes type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getPointer() {
        return pointer;
    }

    public String getLastLobbyToken() {
        return lastLobbyToken;
    }

    public void setLastLobbyToken(String lastLobbyToken) {
        this.lastLobbyToken = lastLobbyToken;
    }

    public void setPointer(Long pointer) {
        this.pointer = pointer;
    }

    public List<Long> getFoundActivities() {
        return (new ArrayList<>(foundActivities.stream().map(LongWrapper::getId).collect(Collectors.toList())));
    }

    public void setFoundActivities(LinkedHashSet<LongWrapper> foundActivities) {
        this.foundActivities = foundActivities;
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
