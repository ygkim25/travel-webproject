/*const loginBtn = document.querySelector('#login-btn');
const popup = document.querySelector('.popup');
const closeBtn = document.querySelector('.close-btn');

loginBtn.addEventListener('click', () => {
    popup.classList.add('active');
    
})
closeBtn.addEventListener('click', () => {
    popup.classList.remove('active');
})
*/


// 레이어 팝업 열기
document.getElementById('showPopup').addEventListener('click', function() {
	window.history.pushState({}, 'Login', '/main/login');
    document.getElementById('popupContainer').style.display = 'block';
});

// 레이어 팝업 닫기
document.getElementById('closePopup').addEventListener('click', function() {
	window.history.pushState({}, 'Main', '/main');
    document.getElementById('popupContainer').style.display = 'none';
});
/**
 * 
 */
const signUpButton = document.getElementById('signUp');
const signInButton = document.getElementById('signIn');
const container = document.getElementById('container');

signUpButton.addEventListener('click', () => {
  container.classList.add("right-panel-active");
});

signInButton.addEventListener('click', () => {
  container.classList.remove("right-panel-active");
})

document.getElementById('closePopup').addEventListener('click', function() {
    popup.classList.remove('active');
});

//function login() {
//    const email = document.querySelector('input[name="email"]').value;
//    const password = document.querySelector('input[name="password"]').value;
//    
//	alert(1);
//	
//    fetch('/login', {
//		
//        method: 'POST',
//        headers: {
//            'Content-Type': 'application/json',
//        },
//        body: JSON.stringify({ email, password }),
//        
//    })   
//    .then(response => response.json())
//    .then(data => {
//        if (data.success) {
//            // 로그인 성공 시 사용자 정보를 표시
//            document.getElementById('authButtons').innerHTML = `
//                <span>${data.username}님</span>
//                <button class="btn btn-outline-light" type="button" onclick="logout()">로그아웃</button>
//            `;
//            alert(2);
//            document.getElementById('popupContainer').style.display = 'none';
//        } else {
//            alert('로그인 실패. 다시 시도해주세요.');
//        }
//    })
//    .catch(error => {
//        console.error('Error:', error);
//    });
//}
//
//function logout() {
//    fetch('/logout', {
//        method: 'POST',
//        headers: {
//            'Content-Type': 'application/json',
//        }
//    })
//    .then(() => {
//        document.getElementById('authButtons').innerHTML = `
//            <button class="btn btn-outline-light" type="submit" id="showPopup">로그인/회원가입</button>
//        `;
//    })
//    .catch(error => {
//        console.error('Error:', error);
//    });
//}
