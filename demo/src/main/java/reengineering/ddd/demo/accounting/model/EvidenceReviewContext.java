package reengineering.ddd.demo.accounting.model;

import io.github.jayclock.smartdomain.core.context.ContextSwitcher;

public interface EvidenceReviewContext
    extends ContextSwitcher<Operator, SourceEvidence<?>, EvidenceReviewer> {}
