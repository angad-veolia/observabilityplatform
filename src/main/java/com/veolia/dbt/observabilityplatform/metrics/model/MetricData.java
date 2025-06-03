package com.veolia.dbt.observabilityplatform.metrics.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity //we will mark this class as an entity to be persisted in the database
@Table(name = "metric_data") //if not specified, the table name will be the class name
@Data // use Lombok to generate getters, setters, toString, equals, and hashCode methods
public class MetricData {

    @Id // this field will be the primary key
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-generate the ID value
    //strategy here is set to IDENTITY, which means the database will generate a unique value for this field
    private Long id;
    
    @Column(nullable = false)
    private String metricName;
    
    @Column(nullable = false)
    private String resourceId;
    
    @Column(nullable = false)
    private Double value;
    
    @Column(nullable = false)
    private Instant timestamp;
    
    @Column
    private String unit;
    
    @Column
    private String namespace;
    
}
