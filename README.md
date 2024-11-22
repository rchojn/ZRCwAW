1. Process rejestracji
```sequenceDiagram
    Frontend->>Cognito: 1. Rejestracja użytkownika
    Cognito-->>Frontend: 2. Zwraca ID użytkownika
    Frontend->>Backend: 3. Synchronizacja danych (/sync-user)
    Backend->>PostgreSQL: 4. Zapisuje użytkownika
```


2. Dlaczego tak to robimy?:
- Bezpieczeństwo: Amazon Cognito zajmuje się hasłami i tokenami
- Wygoda: Mamy własną bazę danych z użytkownikami
- Szybkość: Nie musimy za każdym razem pytać Cognito o dane
- Niezawodność: Aplikacja działa nawet gdy Cognito ma problemy
3. Co się dzieje krok po kroku:
- Użytkownik wypełnia formularz rejestracji
- Dane idą do Amazon Cognito (bezpieczne przechowywanie haseł)
4. Po udanej rejestracji, frontend wysyła dane do naszego backendu
- Backend zapisuje podstawowe dane w PostgreSQL
- Użytkownik może się teraz logować
5.Korzyści tego rozwiązania:
- Nie musimy martwić się o bezpieczeństwo haseł
- Łatwo możemy dodawać własne pola do użytkownika
- Szybkie zapytania do bazy danych
- Możemy łatwo rozszerzać funkcjonalność
