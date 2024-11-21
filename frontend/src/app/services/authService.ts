import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {BehaviorSubject, catchError, first, from, map, Observable, of, switchMap, throwError} from 'rxjs';
import { User } from '../models/user';
import { signUp, signIn, signOut, getCurrentUser, AuthUser, fetchAuthSession, SignInOutput, SignUpOutput} from '@aws-amplify/auth';


const authApiPrefix = 'http://localhost:8080/api/auth';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // private loginSuccessSubject: BehaviorSubject<boolean>;
  // public loginSuccess$: Observable<boolean>;
  // private registerSuccessSubject: BehaviorSubject<boolean>;
  // public registerSuccess$: Observable<boolean>;

  // constructor(private readonly http: HttpClient) {
  //   this.loginSuccessSubject = new BehaviorSubject<boolean>(false);
  //   this.loginSuccess$ = this.loginSuccessSubject.asObservable();
  //   this.registerSuccessSubject = new BehaviorSubject<boolean>(false);
  //   this.registerSuccess$ = this.registerSuccessSubject.asObservable();
  // }

  constructor(private http: HttpClient){}


  login(email: string, password: string): Observable<SignInOutput> {
    return from(signIn({username: email,password}))
      .pipe(
        switchMap(async (result: SignInOutput) => {
          const session = await fetchAuthSession();
          if (session.tokens){
            localStorage.setItem('accessToken', session.tokens.accessToken.toString());
            // @ts-ignore
            localStorage.setItem('idToken', session.tokens.idToken.toString());
          }
        return result;
        })
    );
  }

  logout(): Observable<void> {
    return from(signOut()).pipe(
      map(() => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('idToken');
      }),
      catchError((error) => {
        console.error('Error signing out:', error);
        return throwError(() => error);
      })
    );
  }


  async isAuthenticated(): Promise<boolean> {
    try{
      const user = await getCurrentUser();
      return !!user;
    } catch {
      return false;
    }
  }


  getAccessToken(): string | null {
    if (typeof window !== 'undefined' && window.localStorage) {
      return localStorage.getItem('accessToken');
    }
    return null;
  }


  getIdToken(): string | null {
    if (typeof window !== 'undefined' && window.localStorage) {
      return localStorage.getItem('idToken');
    }
    return null;
  }

  // register(email: string, password: string, additionalAttributes?: { [key: string]: any }): Observable<AuthUser> {
  //   return from(signUp({
  //     username: email,
  //     password: password,
  //     attributes: additionalAttributes
  //   })).pipe(
  //     map((result) => {
  //       console.log('User registered successfully:', result);
  //       return result.user;
  //     }),
  //     catchError((error) => {
  //       console.error('Registration error:', error);
  //       return throwError(() => error);
  //     })
  //   );
  // }

  register(firstname: string, surname: string, login: string, password: string, isSeller: boolean): Observable<any>{
    return from(signUp({
      username: login,
      password,
      options: {
          userAttributes: {
          email: login,
          'given_name': firstname,
          'family_name': surname,
          'custom:isSeller': isSeller.toString()
        }
      }
    })).pipe(
      map((result) => {
        console.log('User register successfully:', result);
        console.log('type isSeller: ', typeof(isSeller))
        return result;
      }),
      catchError((error) =>{
        console.error('Registration error:', error);
        return throwError(()=> error);
      })
    );
  }


  // register(firstname: string, surname: string, login: string, password: string, isSeller: boolean): Observable<User> {
  //   return this.http.post<User>(`${authApiPrefix}/register`, { firstname, surname, login, password, isSeller })
  // }

  getAuthHeaders(): HttpHeaders {
    const accessToken = this.getAccessToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${accessToken}`
    });
  }

  // logout(): Observable<void> {
  //   localStorage.removeItem('accessToken');
  //   return of(undefined);
  // }

  // getAccessToken(): string | null {
  //   if (typeof window !== 'undefined' && window.localStorage) {
  //     return localStorage.getItem('accessToken');
  //   }
  //   return null;
  // }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${authApiPrefix}/current-user`, { headers: this.getAuthHeaders() });
  }
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${authApiPrefix}/users`, { headers: this.getAuthHeaders() });
  }

}
