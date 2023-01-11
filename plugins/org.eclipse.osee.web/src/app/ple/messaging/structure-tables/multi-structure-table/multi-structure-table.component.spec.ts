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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';

import { MultiStructureTableComponent } from './multi-structure-table.component';
import { RouterTestingModule } from '@angular/router/testing';
import { CurrentStateServiceMock } from '../../shared/testing/current-structure.service.mock';
import { CurrentStructureMultiService } from '../../shared/services/ui/current-structure-multi.service';
import { MULTI_STRUCTURE_SERVICE } from '../../shared/tokens/injection/structure/multi';
import { STRUCTURE_SERVICE_TOKEN } from '../../shared/tokens/injection/structure/token';
import { StructureTableComponentMock } from '../lib/tables/structure-table/structure-table.component.mock';
import { AsyncPipe } from '@angular/common';

let loader: HarnessLoader;

describe('MessageElementInterfaceComponent', () => {
	let component: MultiStructureTableComponent;
	let fixture: ComponentFixture<MultiStructureTableComponent>;

	beforeEach(async () => {
		await TestBed.overrideComponent(MultiStructureTableComponent, {
			set: {
				imports: [
					AsyncPipe,
					StructureTableComponentMock,
					RouterTestingModule,
				],
				providers: [
					{
						provide: CurrentStructureMultiService,
						useValue: CurrentStateServiceMock,
					},
					{
						provide: STRUCTURE_SERVICE_TOKEN,
						useValue: MULTI_STRUCTURE_SERVICE,
					},
				],
			},
		}).compileComponents();
	});

	beforeEach(() => {
		fixture = TestBed.createComponent(MultiStructureTableComponent);
		component = fixture.componentInstance;
		loader = TestbedHarnessEnvironment.loader(fixture);
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
