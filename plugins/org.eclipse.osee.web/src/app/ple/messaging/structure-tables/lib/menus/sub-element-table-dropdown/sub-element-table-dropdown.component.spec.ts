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
import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { NgIf, AsyncPipe } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { RouterLink } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { CurrentStateServiceMock } from '../../../../shared/testing/current-structure.service.mock';
import { enumerationUiServiceMock } from '../../../../shared/testing/enumeration-ui.service.mock';
import { CurrentStructureService } from '../../../../shared/services/ui/current-structure.service';
import { EnumerationUIService } from '../../../../shared/services/ui/enumeration-ui.service';
import { STRUCTURE_SERVICE_TOKEN } from '../../../../shared/tokens/injection/structure/token';
import { MatButtonHarness } from '@angular/material/button/testing';

import { SubElementTableDropdownComponent } from './sub-element-table-dropdown.component';
import { MatMenuItemHarness } from '@angular/material/menu/testing';
import { elementsMock } from '../../../../shared/testing/element.response.mock';
import { preferencesUiServiceMock } from '../../../../shared/services/ui/preferences-ui-service.mock';
import { PreferencesUIService } from '../../../../shared/services/ui/preferences-ui.service';

describe('SubElementTableDropdownComponent', () => {
	let component: SubElementTableDropdownComponent;
	let fixture: ComponentFixture<SubElementTableDropdownComponent>;
	let loader: HarnessLoader;
	let service: CurrentStructureService;

	beforeEach(async () => {
		await TestBed.overrideComponent(SubElementTableDropdownComponent, {
			set: {
				imports: [
					NgIf,
					AsyncPipe,
					RouterTestingModule,
					RouterLink,
					MatMenuModule,
					MatIconModule,
					MatDialogModule,
					MatFormFieldModule,
				],
				providers: [
					{
						provide: STRUCTURE_SERVICE_TOKEN,
						useValue: CurrentStateServiceMock,
					},
					{
						provide: CurrentStructureService,
						useValue: CurrentStateServiceMock,
					},
					{
						provide: EnumerationUIService,
						useValue: enumerationUiServiceMock,
					},
				],
			},
		})
			.configureTestingModule({
				imports: [SubElementTableDropdownComponent],
				providers: [
					{
						provide: STRUCTURE_SERVICE_TOKEN,
						useValue: CurrentStateServiceMock,
					},
					{
						provide: CurrentStructureService,
						useValue: CurrentStateServiceMock,
					},
					{
						provide: PreferencesUIService,
						useValue: preferencesUiServiceMock,
					},
				],
			})
			.compileComponents();
		service = TestBed.inject(CurrentStructureService);

		fixture = TestBed.createComponent(SubElementTableDropdownComponent);
		component = fixture.componentInstance;
		component.structure = {
			id: '1734890124',
			name: 'sample structure',
			description: '',
			interfaceMaxSimultaneity: '',
			interfaceMinSimultaneity: '',
			interfaceTaskFileType: 0,
			interfaceStructureCategory: '',
		};
		component.element = elementsMock[0];
		fixture.detectChanges();
		loader = TestbedHarnessEnvironment.loader(fixture);
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
