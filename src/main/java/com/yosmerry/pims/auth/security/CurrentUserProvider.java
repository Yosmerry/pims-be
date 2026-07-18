package com.yosmerry.pims.auth.security;

import com.yosmerry.pims.user.entity.User;

public interface CurrentUserProvider {

  User requireActiveUser();
}
