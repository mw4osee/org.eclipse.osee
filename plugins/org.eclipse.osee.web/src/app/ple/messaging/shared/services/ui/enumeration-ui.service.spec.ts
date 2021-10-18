/*********************************************************************
 * Copyright (c) 2021 Boeing
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
import { TestBed } from '@angular/core/testing';
import { enumerationSetServiceMock } from '../../mocks/enumeration.set.service.mock';
import { EnumerationSetService } from '../http/enumeration-set.service';

import { EnumerationUIService } from './enumeration-ui.service';

describe('EnumerationUIService', () => {
  let service: EnumerationUIService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers:[{provide:EnumerationSetService, useValue: enumerationSetServiceMock}]
    });
    service = TestBed.inject(EnumerationUIService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
