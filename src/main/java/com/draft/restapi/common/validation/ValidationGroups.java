package com.draft.restapi.common.validation;

import javax.validation.groups.Default;

public interface ValidationGroups {
    interface OnCreate extends Default {}
    interface OnRead extends Default {}
    interface OnUpdate extends Default {}
    interface OnDelete extends Default {}
}