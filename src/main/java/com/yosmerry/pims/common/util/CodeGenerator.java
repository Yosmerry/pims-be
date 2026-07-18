package com.yosmerry.pims.common.util;

import com.yosmerry.pims.common.enums.CodeType;

public interface CodeGenerator {

    String next(CodeType type);
}
