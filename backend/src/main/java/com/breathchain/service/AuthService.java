package com.breathchain.service;

import com.breathchain.dto.LoginDTO;
import com.breathchain.dto.RegisterDTO;
import com.breathchain.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    LoginVO register(RegisterDTO dto);
}
