package com.safepathjdbc.etl;

import java.io.InputStream;

public interface EtlProcessor {
    EtlResult process(InputStream inputStream, int sourceId) throws Exception;
}
