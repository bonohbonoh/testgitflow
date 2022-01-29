package com.jeonggolee.helpanimal.domain.crew.service;

import com.jeonggolee.helpanimal.common.exception.CrewNameDuplicateException;
import com.jeonggolee.helpanimal.common.exception.CrewNotFoundException;
import com.jeonggolee.helpanimal.domain.crew.domain.Crew;
import com.jeonggolee.helpanimal.domain.crew.domain.CrewMember;
import com.jeonggolee.helpanimal.domain.crew.dto.CreateCrewDto;
import com.jeonggolee.helpanimal.domain.crew.dto.ReadCrewDetailDto;
import com.jeonggolee.helpanimal.domain.crew.dto.ReadCrewDto;
import com.jeonggolee.helpanimal.domain.crew.dto.UpdateCrewDto;
import com.jeonggolee.helpanimal.domain.crew.enums.CrewMemberRole;
import com.jeonggolee.helpanimal.domain.crew.query.CrewSpecification;
import com.jeonggolee.helpanimal.domain.crew.repository.CrewMemberRepository;
import com.jeonggolee.helpanimal.domain.crew.repository.CrewRepository;
import com.jeonggolee.helpanimal.domain.user.entity.User;
import com.jeonggolee.helpanimal.domain.user.query.UserSpecification;
import com.jeonggolee.helpanimal.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrewService {
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;

    private final CrewSpecification cs;
    private final UserSpecification uss;

    //크루 생성
    public Long createCrew(CreateCrewDto createCrewDto, String masterEmail){
        validateDuplicateCrewName(createCrewDto.getName());

        User user = userRepository.findOne(uss.searchWithEmailEqual(masterEmail))
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 사용자 입니다."));

        CrewMember crewMaster = createCrewMaster(user);
        crewMemberRepository.save(crewMaster);

        Crew crew = createCrewDto.toEntity();
        crew.addCrewMember(crewMaster);

        return crewRepository.save(crew).getId();
    }

    //크루명 중복처리
    private void validateDuplicateCrewName(String crewName){
        if (crewRepository.existsCrewByName(crewName)) {
            throw new CrewNameDuplicateException("이미 존재하는 크루이름입니다.");
        }
    }

    //크루마스터 생성
    private CrewMember createCrewMaster(User user){
        return CrewMember.builder()
                .user(user)
                .role(CrewMemberRole.MASTER)
                .build();
    }

    //크루 전체목록 조회
    public List<ReadCrewDto> readCrewList(int page, int size){
        PageRequest pageRequest = PageRequest.of(page, size);
        return crewRepository.findAll(pageRequest).stream()
                .map(ReadCrewDto::new)
                .collect(Collectors.toList());
    }

    //크루 목록 이름으로 조회
    public List<ReadCrewDto> readCrewListByName(int page, int size, String name){
        PageRequest pageRequest = PageRequest.of(page, size);
        return crewRepository.findAll(cs.searchWithName(name), pageRequest).stream()
                .map(ReadCrewDto::new)
                .collect(Collectors.toList());
    }

    //크루 아이디로 상세조회
    public ReadCrewDetailDto readCrewDetail(Long id){
        Crew crew = crewRepository.findOne(cs.searchWithId(id))
                .orElseThrow(() -> new CrewNotFoundException("존재하지 않는 크루 입니다."));

        return new ReadCrewDetailDto(crew);
    }

    //크루 수정
    public Long updateCrew(UpdateCrewDto updateCrewDto){
        validateDuplicateCrewName(updateCrewDto.getName());

        Crew crew = crewRepository.findOne(cs.searchWithId(updateCrewDto.getId()))
                .orElseThrow(() -> new CrewNotFoundException("존재하지 않는 크루 입니다."));

        if(updateCrewDto.getName() != null)
            crew.updateName(updateCrewDto.getName());

        return crewRepository.save(crew).getId();
    }

    public void deleteCrew(Long id){
        Crew crew = crewRepository.findOne(cs.searchWithId(id))
                .orElseThrow(() -> new CrewNotFoundException("존재하지 않는 크루 입니다."));

        crew.delete();

        crewRepository.save(crew);
    }

}
