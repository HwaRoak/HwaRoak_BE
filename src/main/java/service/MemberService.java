package service;

import com.umc.hwaroak.dto.MemberResponseDTO;

public interface MemberService {

    MemberResponseDTO.InfoDTO getInfo(Long id);
}
