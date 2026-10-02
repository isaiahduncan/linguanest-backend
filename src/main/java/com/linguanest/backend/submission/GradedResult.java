package com.linguanest.backend.submission;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.linguanest.backend.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI-generated feedback for one {@link Submission} - score, markdown feedback, and structured
 * JSONB corrections. One-to-one with {@code submissions} in practice, though not currently
 * enforced by a {@code UNIQUE} constraint on {@code submission_id} (see Open Items in
 * backend-api-spec.md).
 *
 * <p>{@code detailedCorrections} is mapped as a generic {@code Map} (rather than a specific JSON
 * library type) since graded feedback is a nested, variable-shape structure that doesn't cleanly
 * normalize into further relational tables - Hibernate serializes/deserializes it to the
 * {@code jsonb} column via the JSON {@code FormatMapper} on the classpath.
 */
@Entity
@Table(name = "graded_results")
@Getter
@Setter
@NoArgsConstructor
public class GradedResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(name = "feedback_markdown", nullable = false)
    private String feedbackMarkdown;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detailed_corrections", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> detailedCorrections;
}
