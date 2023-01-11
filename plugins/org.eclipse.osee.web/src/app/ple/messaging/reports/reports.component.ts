/*********************************************************************
 * Copyright (c) 2022 Boeing
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
import { Component, OnInit } from '@angular/core';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { from, iif, of } from 'rxjs';
import { filter, scan, startWith, switchMap, tap } from 'rxjs/operators';
import { applic } from 'src/app/types/applicability/applic';
import { ConnectionService } from '../shared/services/http/connection.service';
import { ApplicabilityListService } from '../shared/services/http/applicability-list.service';
import { ReportsService } from '../shared/services/ui/reports.service';
import { connection } from '../shared/types/connection';
import { MimReport } from '../shared/types/Reports';
import { UiService } from '../../../ple-services/ui/ui.service';
import { BranchPickerComponent } from '../../../shared-components/components/branch-picker/branch-picker/branch-picker.component';
import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatOptionModule } from '@angular/material/core';
import { FormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';

@Component({
	selector: 'osee-messaging-reports',
	templateUrl: './reports.component.html',
	styleUrls: ['./reports.component.sass'],
	standalone: true,
	imports: [
		NgIf,
		AsyncPipe,
		RouterLink,
		NgFor,
		FormsModule,
		MatFormFieldModule,
		MatSelectModule,
		MatOptionModule,
		MatCheckboxModule,
		MatButtonModule,
		BranchPickerComponent,
	],
})
export class ReportsComponent implements OnInit {
	constructor(
		private route: ActivatedRoute,
		private routerState: UiService,
		private reportsService: ReportsService,
		private connectionService: ConnectionService,
		private applicService: ApplicabilityListService
	) {}

	ngOnInit(): void {
		this.route.paramMap.subscribe((params) => {
			this.routerState.idValue = params.get('branchId') || '';
			this.routerState.typeValue = params.get('branchType') || '';
		});
	}

	selectedReport: MimReport | undefined = undefined;
	selectedApplic: applic = { id: '-1', name: 'None' };

	branchId = this.reportsService.branchId;
	branchType = this.reportsService.branchType;
	reports = this.reportsService.getReports();
	diffReportRoute = this.reportsService.diffReportRoute;

	reportSelectionText = this.reports.pipe(
		switchMap((reports) =>
			iif(
				() => reports.length > 0,
				of('Select a Report'),
				of('No reports available')
			)
		)
	);

	connections = this.branchId.pipe(
		filter((v) => v !== ''),
		switchMap((branchId) => this.connectionService.getConnections(branchId))
	);

	connectionSelectionText = this.connections.pipe(
		switchMap((connections) =>
			iif(
				() => connections.length > 0,
				of('Select a Connection'),
				of('No connections available')
			)
		)
	);

	applicViews = this.branchId.pipe(
		filter((v) => v !== ''),
		switchMap((branchId) =>
			this.applicService.getViews(branchId).pipe(
				switchMap((applics) =>
					from(applics).pipe(
						startWith({ id: '-1', name: 'None' } as applic),
						scan((acc, curr) => {
							acc.push(curr);
							return acc;
						}, [] as applic[])
					)
				)
			)
		)
	);

	selectReport(event: MatSelectChange) {
		this.selectedReport = event.value;
	}

	getSelectedReport() {
		this.reportsService
			.downloadReport(this.selectedReport, this.selectedApplic.id)
			.subscribe();
	}

	selectConnection(event: MatSelectChange) {
		this.selectedConnection = event.value;
	}

	selectFile(event: Event) {
		const target = event.target as HTMLInputElement;
		if (target.files && target.files.length > 0) {
			const file: File = target.files[0];
			this.reportsService.RequestBodyFile = file;
		}
	}

	get requestBody() {
		return this.reportsService.requestBody.getValue();
	}

	set requestBody(requestBody: string) {
		this.reportsService.RequestBody = requestBody;
	}

	get selectedConnection() {
		return this.reportsService.connection.getValue() as connection;
	}

	set selectedConnection(connection: connection) {
		this.reportsService.Connection = connection;
	}

	get includeDiff() {
		return this.reportsService.includeDiff.value;
	}

	set includeDiff(value: boolean) {
		this.reportsService.IncludeDiff = value;
	}
}

export default ReportsComponent;
