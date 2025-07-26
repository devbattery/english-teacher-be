# English Teacher

<div align="center">
  <img src="https://github.com/user-attachments/assets/ddf05811-8290-427d-992d-47cc0c5c0880" alt="프로젝트 소개 아이콘" width="500"/>
</div>

<br>

> 개발 기간: 2025.05 ~ 진행중  
> 도메인 링크: [https://englishteacher.store](https://englishteacher.store/)  
> 프론트엔드 링크: https://github.com/devbattery/english-teacher-fe  
> 백엔드 링크: https://github.com/devbattery/english-teacher-be

## 개발 인원 소개

|                                                                                  1인 개발                                                                                  |
|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| <a href="https://github.com/devbattery"><img src="https://avatars.githubusercontent.com/devbattery" width="120px;"></a><br/>[<b>정원준</b>](https://github.com/devbattery) |

## 프로젝트 소개

현재 AI의 발전 속도가 기하급수적으로 올라가고 있습니다.

**English Teacher는 이 속도의 흐름을 적극적으로 활용하려고 합니다.**

대한민국 학생이라면 빠르면 유치원생 때부터, 늦으면 초등학생 때부터라도 영어 공부를 시작합니다. 하지만, 막상 성인이 되어도 영어가 어렵다고 느끼는 경우가 많습니다.

**English Teacher의 학습 방향을 따라오시면, 분명 지금까지의 공부보다 더 실전적으로 영어를 친근하게 느끼실 수 있을 겁니다.**

우리의 웹 사이트는 이렇습니다.

1. AI에 막대한 돈을 쏟아붓고 있는 Google의 **Gemini API**를 활용합니다.
2. **왕초보**, **고등학생**, **원어민**, **TOEIC** 총 4가지의 자체적으로 학습시킨 모델을 만나보실 수 있습니다.
3. **PC 버전**, **모바일 버전**을 모두 지원하며 **웹 앱**으로도 사용하실 수 있습니다.
4. 백엔드는 **Spring Boot**를 이용했고, 프론트엔드는 **Vite React**를 이용하여 개발했습니다.

## 시작 가이드

### Requirements

- Java 17
- Npm 10.9.2

### Installation

```shell
$ git clone https://github.com/devbattery/english-teacher-be
```

```shell
$ git clone https://github.com/devbattery/english-teacher-fe
```

#### Backend

```shell
$ cd english-teacher-be
$ ./gradlew clean build
$ java -jar build/libs/english-teacher-0.0.1-SNAPSHOT.jar
```

#### Frontend

```shell
$ cd english-teacher-fe
$ npm run build
$ npm install -g serve
$ serve -s dist
```

## 화면 구성

### HomePage

<table>
  <tr>
    <td align="center"><strong>라이트모드</strong></td>
    <td align="center"><strong>다크모드</strong></td>
  </tr>
  <tr>
    <td align="center">
      <img width="400" alt="Screenshot 2025-07-26 at 21 17 59" src="https://github.com/user-attachments/assets/98e34519-c9f7-4f1d-98b2-66f8c41938a5" />
    </td>
    <td align="center">
      <img width="400" alt="Screenshot 2025-07-26 at 21 18 29" src="https://github.com/user-attachments/assets/805180fa-5b0a-4ac3-86e4-fc702b13f840" />
    </td>
  </tr>
</table>

### Messenger

<table>
  <tr>
    <td align="center"><strong>채팅 목록 페이지</strong></td>
    <td align="center"><strong>채팅 세부 페이지</strong></td>
  </tr>
  <tr>
    <td align="center">
      <img width="400" alt="Screenshot 2025-07-26 at 21 19 59" src="https://github.com/user-attachments/assets/9e28d1f8-ff5f-4c8d-ac26-7aa29a4c4ade" />
    </td>
    <td align="center">
      <img width="400" alt="Screenshot 2025-07-26 at 21 22 28" src="https://github.com/user-attachments/assets/b113479f-ef8d-46e8-a5c9-e8fc604c10df" />
    </td>
  </tr>
</table>

### Daily Contents

<table>
  <tr>
    <td align="center"><strong>컨텐츠 페이지</strong></td>
    <td align="center"><strong>컨텐츠 단어장 생성 기능</strong></td>
  </tr>
  <tr>
    <td align="center">
      <img width="400" alt="Screenshot 2025-07-26 at 21 23 55" src="https://github.com/user-attachments/assets/b73ded9e-f294-40cb-af7b-d0b8422a3e20" />
    </td>
    <td align="center">
      <img width="400" alt="Screenshot 2025-07-26 at 21 24 49" src="https://github.com/user-attachments/assets/e0c50784-eaa6-4d5e-a90f-cc6f7e1849f1" />
    </td>
  </tr>
</table>

## 기능 소개

- TODO
