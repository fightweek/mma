## Test Specification - UserService
***
- [x] 닉네임 변경 (updateNickname)
- [x] 사용자 기본 정보 (getMe)
- [x] 회원 가입 (join)
  - [x] 중복된 이메일로 회원 가입 요청 시, 예외 발생 (CustomErrorCode.DUPLICATED_EMAIL_400)
  - [x] 중복된 닉네임으로 회원 가입 요청 시, 예외 발생 (CustomErrorCode.DUPLICATED_NICKNAME_400)
- [ ] 사용자 상세 정보 (profile)