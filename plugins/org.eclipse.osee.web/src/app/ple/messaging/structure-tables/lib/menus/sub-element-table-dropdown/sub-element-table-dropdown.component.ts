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
import { Component, Inject, Input } from '@angular/core';
import {
	take,
	switchMap,
	iif,
	of,
	filter,
	combineLatest,
	map,
	OperatorFunction,
	tap,
} from 'rxjs';
import { applic } from '../../../../../../types/applicability/applic';
import { difference } from '../../../../../../types/change-report/change-report';
import { EditEnumSetDialogComponent } from '../../../../shared/dialogs/edit-enum-set-dialog/edit-enum-set-dialog.component';
import { EditViewFreeTextFieldDialogComponent } from '../../../../shared/dialogs/edit-view-free-text-field-dialog/edit-view-free-text-field-dialog.component';
import { EditViewFreeTextDialog } from '../../../../shared/types/EditViewFreeTextDialog';
import { enumerationSet } from '../../../../shared/types/enum';
import { AddElementDialog } from '../../dialogs/add-element-dialog/add-element-dialog';
import { AddElementDialogComponent } from '../../dialogs/add-element-dialog/add-element-dialog.component';
import { DefaultAddElementDialog } from '../../dialogs/add-element-dialog/add-element-dialog.default';
import { RemoveElementDialogData } from '../../dialogs/remove-element-dialog/remove-element-dialog';
import { RemoveElementDialogComponent } from '../../dialogs/remove-element-dialog/remove-element-dialog.component';
import { element } from '../../../../shared/types/element';
import { structure } from '../../../../shared/types/structure';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Router, RouterLink } from '@angular/router';
import { UiService } from '../../../../../../ple-services/ui/ui.service';
import { CurrentStructureService } from '../../../../shared/services/ui/current-structure.service';
import { EnumerationUIService } from '../../../../shared/services/ui/enumeration-ui.service';
import { HeaderService } from '../../../../shared/services/ui/header.service';
import { STRUCTURE_SERVICE_TOKEN } from '../../../../shared/tokens/injection/structure/token';
import { AsyncPipe, NgIf } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';

/**
 * Required attributes:
 * element
 * structure
 * header
 * branchId
 * branchType
 * editMode
 */
@Component({
	selector:
		'osee-sub-element-table-dropdown[element][structure][header][branchId][branchType][editMode]',
	standalone: true,
	imports: [
		NgIf,
		AsyncPipe,
		RouterLink,
		MatMenuModule,
		MatIconModule,
		MatDialogModule,
		MatFormFieldModule,
	],
	templateUrl: './sub-element-table-dropdown.component.html',
	styleUrls: ['./sub-element-table-dropdown.component.sass'],
})
export class SubElementTableDropdownComponent {
	@Input() element: element = {
		id: '-1',
		name: '',
		description: '',
		notes: '',
		interfaceElementIndexEnd: 0,
		interfaceElementIndexStart: 0,
		interfaceElementAlterable: false,
		units: '',
		enumLiteral: '',
		autogenerated: true,
	};

	@Input() structure: structure = {
		id: '-1',
		name: '',
		description: '',
		interfaceMaxSimultaneity: '',
		interfaceMinSimultaneity: '',
		interfaceTaskFileType: 0,
		interfaceStructureCategory: '',
	};

	@Input() header!: string;
	@Input() field?: string | number | boolean | applic;

	@Input('branchId') _branchId: string = '';
	@Input('branchType') _branchType: string = '';

	@Input() editMode: boolean = false;

	constructor(
		private _ui: UiService,
		private router: Router,
		public dialog: MatDialog,
		@Inject(STRUCTURE_SERVICE_TOKEN)
		private structureService: CurrentStructureService,
		private headerService: HeaderService,
		private enumSetService: EnumerationUIService
	) {}
	removeElement(element: element, structure: structure) {
		const dialogData: RemoveElementDialogData = {
			elementId: element.id,
			structureId: structure.id,
			elementName: element.name,
		};
		this.dialog
			.open(RemoveElementDialogComponent, {
				data: dialogData,
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((dialogResult: string) =>
					iif(
						() => dialogResult === 'ok',
						this.structureService.removeElementFromStructure(
							element,
							structure
						),
						of()
					)
				)
			)
			.subscribe();
	}
	deleteElement(element: element) {
		//open dialog, yes/no if yes -> this.structures.deleteElement()
		const dialogData: RemoveElementDialogData = {
			elementId: element.id,
			structureId: '',
			elementName: element.name,
		};
		this.dialog
			.open(RemoveElementDialogComponent, {
				data: dialogData,
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((dialogResult: string) =>
					iif(
						() => dialogResult === 'ok',
						this.structureService.deleteElement(element),
						of()
					)
				)
			)
			.subscribe();
	}
	openAddElementDialog(structure: structure, afterElement?: string) {
		const dialogData = new DefaultAddElementDialog(
			structure?.id || '',
			structure?.name || ''
		);
		let dialogRef = this.dialog.open(AddElementDialogComponent, {
			data: dialogData,
		});
		let createElement = dialogRef.afterClosed().pipe(
			take(1),
			filter(
				(val) =>
					(val !== undefined || val !== null) &&
					val?.element !== undefined
			),
			switchMap((value: AddElementDialog) =>
				iif(
					() =>
						value.element.id !== '-1' &&
						value.element.id.length > 0,
					this.structureService
						.relateElement(
							structure.id,
							value.element.id,
							afterElement || 'end'
						)
						.pipe(
							switchMap((transaction) =>
								combineLatest([
									this._ui.isLoading,
									of(transaction),
								]).pipe(
									filter(
										([loading, transaction]) =>
											loading !== 'false'
									),
									take(1),
									map(([loading, transaction]) => {
										this.router.navigate([], {
											fragment: 'a' + value.element.id,
										});
									})
								)
							)
						),
					this.structureService
						.createNewElement(
							value.element,
							structure.id,
							value.type.id as string,
							afterElement || 'end'
						)
						.pipe(
							switchMap((transaction) =>
								combineLatest([
									this._ui.isLoading,
									of(transaction),
								]).pipe(
									filter(
										([loading, transaction]) =>
											loading !== 'false'
									),
									take(1),
									map(([loading, transaction]) => {
										this.router.navigate([], {
											fragment:
												'a' +
												(transaction.results.ids[0] ||
													afterElement ||
													''),
										});
									})
								)
							)
						)
				)
			)
		);
		createElement.subscribe();
	}
	openEnumDialog(id: string) {
		this.dialog
			.open(EditEnumSetDialogComponent, {
				data: {
					id: id,
					isOnEditablePage: this.editMode,
				},
			})
			.afterClosed()
			.pipe(
				filter((x) => x !== undefined) as OperatorFunction<
					enumerationSet | undefined,
					enumerationSet
				>,
				take(1),
				switchMap(({ enumerations, ...changes }) =>
					iif(
						() => this.editMode,
						this.enumSetService.changeEnumSet(
							changes,
							enumerations
						),
						of()
					)
				)
			)
			.subscribe();
	}

	openDescriptionDialog(
		description: string,
		elementId: string,
		structureId: string
	) {
		this.dialog
			.open(EditViewFreeTextFieldDialogComponent, {
				data: {
					original: JSON.parse(JSON.stringify(description)) as string,
					type: 'Description',
					return: description,
				},
				minHeight: '60%',
				minWidth: '60%',
			})
			.afterClosed()
			.pipe(
				take(1),
				tap((v) => console.log(v)),
				switchMap((response: EditViewFreeTextDialog | string) =>
					iif(
						() =>
							response === 'ok' ||
							response === 'cancel' ||
							response === undefined,
						//do nothing
						of(),
						//change description
						this.structureService.partialUpdateElement(
							{
								id: elementId,
								description: (
									response as EditViewFreeTextDialog
								).return,
							},
							this.structure.id
						)
					)
				)
			)
			.subscribe();
	}

	/**
   * 
   Need to verify if type is required
   */
	openEnumLiteralDialog(enumLiteral: string, elementId: string) {
		this.dialog
			.open(EditViewFreeTextFieldDialogComponent, {
				data: {
					original: JSON.parse(JSON.stringify(enumLiteral)) as string,
					type: 'Enum Literal',
					return: enumLiteral,
				},
				minHeight: '60%',
				minWidth: '60%',
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((response: EditViewFreeTextDialog | string) =>
					iif(
						() =>
							response === 'ok' ||
							response === 'cancel' ||
							response === undefined,
						//do nothing
						of(),
						//change description
						this.structureService.partialUpdateElement(
							{
								id: elementId,
								description: (
									response as EditViewFreeTextDialog
								).return,
							},
							this.structure.id
						)
					)
				)
			)
			.subscribe();
	}

	openNotesDialog(notes: string, elementId: string, structureId: string) {
		this.dialog
			.open(EditViewFreeTextFieldDialogComponent, {
				data: {
					original: JSON.parse(JSON.stringify(notes)) as string,
					type: 'Notes',
					return: notes,
				},
				minHeight: '60%',
				minWidth: '60%',
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((response: EditViewFreeTextDialog | string) =>
					iif(
						() =>
							response === 'ok' ||
							response === 'cancel' ||
							response === undefined,
						//do nothing
						of(),
						//change notes
						this.structureService.partialUpdateElement(
							{
								id: elementId,
								notes: (response as EditViewFreeTextDialog)
									.return,
							},
							this.structure.id
						)
					)
				)
			)
			.subscribe();
	}

	getHeaderByName(value: string) {
		return this.headerService.getHeaderByName(value, 'element');
	}

	viewDiff(value: difference, header: string) {
		this.structureService.sideNav = {
			opened: true,
			field: header,
			currentValue: value.currentValue as string | number | applic,
			previousValue: value.previousValue as
				| string
				| number
				| applic
				| undefined,
			transaction: value.transactionToken,
		};
	}
}
