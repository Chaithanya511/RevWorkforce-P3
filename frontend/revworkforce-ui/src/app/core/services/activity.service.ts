import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { API_CONFIG, getApiUrl } from '../config/api.config';

@Injectable({ providedIn: 'root' })
export class ActivityService {
  constructor(private http: HttpClient) {}

  getAllActivities() {
    return this.http.get<any>(getApiUrl(API_CONFIG.REPORTS.ACTIVITY_LOGS))
      .pipe(map((res: any) => res.data || res));
  }

  getUserActivities(userId: number) {
    return this.http.get<any>(getApiUrl(API_CONFIG.REPORTS.ACTIVITY_BY_USER(userId)))
      .pipe(map((res: any) => res.data || res));
  }
}
