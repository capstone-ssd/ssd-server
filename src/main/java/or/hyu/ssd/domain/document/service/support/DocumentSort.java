package or.hyu.ssd.domain.document.service.support;

public enum DocumentSort {
    LATEST,      // createdAt DESC
    OLDEST,      // createdAt ASC
    NAME,        // summary ASC (nulls last)
    MODIFIED     // updatedAt DESC
}

