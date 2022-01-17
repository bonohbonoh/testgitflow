package com.jeonggolee.helpanimal.domain.recruitment.service;

import com.jeonggolee.helpanimal.common.execption.AnimalNotFoundException;
import com.jeonggolee.helpanimal.common.execption.RecruitmentNotFoundException;
import com.jeonggolee.helpanimal.common.execption.UserNotFoundException;
import com.jeonggolee.helpanimal.domain.recruitment.dto.request.RecruitmentRegistDto;
import com.jeonggolee.helpanimal.domain.recruitment.dto.request.RecruitmentUpdateDto;
import com.jeonggolee.helpanimal.domain.recruitment.dto.response.RecruitmentDetailDto;
import com.jeonggolee.helpanimal.domain.recruitment.dto.response.RecruitmentListDto;
import com.jeonggolee.helpanimal.domain.recruitment.entity.Animal;
import com.jeonggolee.helpanimal.domain.recruitment.entity.Recruitment;
import com.jeonggolee.helpanimal.domain.recruitment.exception.RecruitmentNotOwnerException;
import com.jeonggolee.helpanimal.domain.recruitment.repository.AnimalRepository;
import com.jeonggolee.helpanimal.domain.recruitment.repository.RecruitmentRepository;
import com.jeonggolee.helpanimal.domain.user.dto.UserInfoReadDto;
import com.jeonggolee.helpanimal.domain.user.entity.User;
import com.jeonggolee.helpanimal.domain.user.repository.UserRepository;
import com.jeonggolee.helpanimal.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentService {
    private final RecruitmentRepository recruitmentRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AnimalRepository animalRepository;
    /**
     * 공고등록
     */
    public Long registRecruitment(RecruitmentRegistDto dto) {
        User author = validateUser(dto.getEmail());
        Animal animal = validateAnimal(dto.getAnimal());
        return recruitmentRepository.save(dto.toEntity(author,animal)).getId();
    }

    /**
     * 공고 조회 페이징
     */
    public Page<RecruitmentListDto> getRecriutmentListWithPaging(Pageable pageable) {
        return recruitmentRepository.findAllByDeletedAtIsNull(pageable)
                .map(recruitment -> new RecruitmentListDto().builder()
                        .id(recruitment.getId())
                        .name(recruitment.getName())
                        .recruitmentType(recruitment.getRecruitmentType())
                        .author(recruitment.getUser().getNickname())
                        .content(recruitment.getContent())
                        .animalType(recruitment.getAnimal().getName())
                        .participant(recruitment.getParticipant())
                        .imageUrl(recruitment.getImageUrl())
                        .recruitmentMethod(recruitment.getRecruitmentMethod())
                        .build());
    }

    /**
     * 공고 상세조회
     */
    public RecruitmentDetailDto getRecruitmentDetail(Long id) {
        return RecruitmentDetailDto.convertDto(getRecruitment(id));
    }

    /**
     * 공고 수정
     */
    public void updateRecruitment(Long id, RecruitmentUpdateDto dto) throws Exception {
        Recruitment recruitment = getRecruitment(id);

        validateAuthor(recruitment.getUser());

        recruitment.updateRecruitmentName(dto.getName());
        recruitment.updateContent(dto.getContent());
        recruitment.updateParticipant(dto.getParticipant());
        recruitment.updateImageUrl(dto.getImageUrl());

        recruitmentRepository.save(recruitment);
    }

    /**
     * 공고 삭제
     */
    public void deleteRecruitment(Long id) throws Exception {
        Recruitment recruitment = getRecruitment(id);
        validateAuthor(recruitment.getUser());
        recruitment.delete();
        recruitmentRepository.save(recruitment);
    }

    private User validateUser(String email) {
        return userRepository.findByEmailAndDeletedAtNull(email).orElseThrow(
                ()-> new UserNotFoundException("해당 회원이 존재하지 않습니다."));
    }

    private Animal validateAnimal(String animal) {
        return animalRepository.findByNameAndDeletedAtNull(animal).orElseThrow(
                ()-> new AnimalNotFoundException("해당 동물은 존재하지 않습니다."));
    }

    private Recruitment getRecruitment(Long id) {
        return recruitmentRepository.findById(id).orElseThrow(
                ()-> new RecruitmentNotFoundException("해당 공고를 찾을 수 없습니다."));
    }

    private void validateAuthor(User user) throws Exception {
        UserInfoReadDto userInfoReadDto = userService.userInfoReadDto();
        if (userInfoReadDto.getUserId() != user.getUserId()) {
            throw new RecruitmentNotOwnerException("해당 공고의 작성자가 아닙니다.");
        }
    }
}
