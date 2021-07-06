import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { PlConfigUserService } from './pl-config-user.service';

describe('PlConfigUserService', () => {
  let service: PlConfigUserService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports:[HttpClientTestingModule],
      });
    service = TestBed.inject(PlConfigUserService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
