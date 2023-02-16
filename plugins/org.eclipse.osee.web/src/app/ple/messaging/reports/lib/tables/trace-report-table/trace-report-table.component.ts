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
import { AfterViewInit, Component, Input, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NodeTraceReportItem } from 'src/app/ple/messaging/shared/types/NodeTraceReport';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { HighlightFilteredTextDirective } from '@osee/shared/utils';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { HeaderKeysEnum, HeaderService } from '@osee/messaging/shared';

@Component({
	selector: 'osee-trace-report-table',
	standalone: true,
	imports: [
		CommonModule,
		FormsModule,
		HighlightFilteredTextDirective,
		MatFormFieldModule,
		MatIconModule,
		MatInputModule,
		MatSortModule,
		MatTableModule,
	],
	templateUrl: './trace-report-table.component.html',
	styleUrls: ['./trace-report-table.component.scss'],
})
export class TraceReportTableComponent implements AfterViewInit {
	@Input() data: NodeTraceReportItem[] = [];

	@ViewChild(MatSort) sort!: MatSort;

	dataSource: MatTableDataSource<NodeTraceReportItem>;

	constructor(private headerService: HeaderService) {
		this.dataSource = new MatTableDataSource(this.data);
	}

	filterPredicate(data: NodeTraceReportItem, filter: string) {
		const filterLower = filter.toLowerCase();
		if (
			data.name.toLowerCase().includes(filterLower) ||
			data.artifactType.toLowerCase().includes(filterLower)
		) {
			return true;
		}
		for (const rel of data.relatedItems) {
			if (
				rel.name.toLowerCase().includes(filterLower) ||
				rel.artifactType.toLowerCase().includes(filterLower)
			) {
				return true;
			}
		}
		return false;
	}

	applyFilter(event: Event) {
		const filterValue = (event.target as HTMLInputElement).value;
		this.dataSource.filterPredicate = this.filterPredicate;
		this.dataSource.filter = filterValue;
	}

	ngAfterViewInit() {
		this.dataSource = new MatTableDataSource(this.data);
		this.dataSource.sort = this.sort;
	}

	getTableHeaderByName(header: string) {
		return this.headerService.getTableHeaderByName(
			header,
			HeaderKeysEnum.NODE_TRACE_REQ
		);
	}

	headers: (keyof NodeTraceReportItem)[] = [
		'name',
		'artifactType',
		'relatedItems',
	];
}
