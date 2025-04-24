import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {

    if (req.url.endsWith('/login')) {
      return next.handle(req);
    }

    const jwtToken = localStorage.getItem('jwt'); 
    
    if (jwtToken) {
      
      const cloned = req.clone({
        setHeaders: {
          Authorization: `Bearer ${jwtToken}`
        }
      });
      return next.handle(cloned);
    }

    return next.handle(req);
  }
}
