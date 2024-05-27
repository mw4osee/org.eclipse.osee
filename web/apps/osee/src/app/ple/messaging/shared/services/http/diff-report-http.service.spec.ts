/*********************************************************************
 * Copyright (c) 2023 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DiffReportHttpService } from './diff-report-http.service';
import {
	provideHttpClient,
	withInterceptorsFromDi,
} from '@angular/common/http';

describe('DiffReportHttpService', () => {
	let service: DiffReportHttpService;

	beforeEach(() => {
		TestBed.configureTestingModule({
			imports: [],
			providers: [
				provideHttpClient(withInterceptorsFromDi()),
				provideHttpClientTesting(),
			],
		});
		service = TestBed.inject(DiffReportHttpService);
	});

	it('should be created', () => {
		expect(service).toBeTruthy();
	});
});
