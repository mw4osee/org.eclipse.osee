/*********************************************************************
 * Copyright (c) 2024 Boeing
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
import { CurrentGraphService } from '../../services/current-graph.service';
import { graphServiceMock } from '../../testing/current-graph.service.mock';
import { GraphNodeAddMenuComponent } from './graph-node-add-menu.component';
import { nodeData } from '@osee/messaging/shared/types';
import { nodesMock } from '@osee/messaging/shared/testing';

describe('GraphNodeAddMenuComponent', () => {
	let component: GraphNodeAddMenuComponent;
	let fixture: ComponentFixture<GraphNodeAddMenuComponent>;
	const testNode: nodeData = nodesMock[0];
	beforeEach(async () => {
		await TestBed.configureTestingModule({
			imports: [GraphNodeAddMenuComponent],
			providers: [
				{ provide: CurrentGraphService, useValue: graphServiceMock },
			],
		}).compileComponents();

		fixture = TestBed.createComponent(GraphNodeAddMenuComponent);
		component = fixture.componentInstance;
		fixture.componentRef.setInput('editMode', true);
		fixture.componentRef.setInput('data', testNode);
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
