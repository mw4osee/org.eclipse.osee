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
import {
	ChangeDetectionStrategy,
	Component,
	computed,
	effect,
	inject,
	input,
	model,
	viewChild,
} from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { MatLabel } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import {
	MatMenu,
	MatMenuContent,
	MatMenuItem,
	MatMenuTrigger,
} from '@angular/material/menu';
import { RouterLink } from '@angular/router';
import { applic, applicabilitySentinel } from '@osee/applicability/types';
import { EditViewFreeTextFieldDialogComponent } from '@osee/messaging/shared/dialogs/free-text';
import { STRUCTURE_SERVICE_TOKEN } from '@osee/messaging/shared/tokens';
import {
	EditViewFreeTextDialog,
	displayableStructureFields,
	structure,
	structureWithChanges,
} from '@osee/messaging/shared/types';
import { writableSlice } from '@osee/shared/utils';
import { filter, iif, of, switchMap, take } from 'rxjs';
import { AddStructureDialog } from '../../dialogs/add-structure-dialog/add-structure-dialog';
import { AddStructureDialogComponent } from '../../dialogs/add-structure-dialog/add-structure-dialog.component';
import { DeleteStructureDialogComponent } from '../../dialogs/delete-structure-dialog/delete-structure-dialog.component';
import { RemoveStructureDialogComponent } from '../../dialogs/remove-structure-dialog/remove-structure-dialog.component';

@Component({
	selector: 'osee-structure-menu',
	standalone: true,
	imports: [
		RouterLink,
		MatMenuItem,
		MatIcon,
		MatMenuTrigger,
		MatMenuContent,
		MatLabel,
		MatMenu,
	],
	template: ` <div
			style="visibility: hidden; position: fixed"
			[style.left]="menuPositionX()"
			[style.top]="menuPositionY()"
			[matMenuTriggerFor]="contextMenu"></div>
		<mat-menu
			#contextMenu="matMenu"
			(closed)="open.set(false)">
			<ng-template matMenuContent>
				@if (singleStructureAvailable()) {
					<a
						mat-menu-item
						target="_blank"
						[routerLink]="url()"
						queryParamsHandling="merge"
						><mat-icon class="tw-text-osee-blue-9"
							>open_in_new</mat-icon
						>Open structure table in new tab</a
					>
				}
				@if (!structure().autogenerated) {
					<button
						mat-menu-item
						(click)="openDescriptionDialog()"
						data-cy="structure-open-description-btn">
						<mat-icon class="tw-text-osee-blue-9"
							>description</mat-icon
						>Open Description
					</button>
				}
				@if (canDisplayViewDiff()) {
					<button
						mat-menu-item
						(click)="viewDiff(true)"
						data-cy="structure-diff-btn">
						<mat-icon
							class="tw-text-osee-yellow-10 dark:tw-text-osee-amber-9"
							>pageview</mat-icon
						>View Diff
					</button>
				}
				@if (isEditing() && !structure().deleted) {
					@if (!structure().autogenerated) {
						<button
							mat-menu-item
							(click)="removeStructureDialog()"
							data-cy="structure-remove-btn">
							<mat-icon class="tw-text-osee-red-9"
								>remove_circle_outline</mat-icon
							>Remove structure from submessage
						</button>
						<button
							mat-menu-item
							(click)="deleteStructureDialog()"
							data-cy="structure-delete-btn">
							<mat-icon class="tw-text-osee-red-9"
								>delete_forever</mat-icon
							>Delete structure globally
						</button>
						<button
							mat-menu-item
							(click)="insertStructure(structure().id)"
							data-cy="structure-insert-after-btn">
							<mat-icon class="tw-text-osee-green-9">add</mat-icon
							>Insert structure after
						</button>
						<button
							mat-menu-item
							(click)="insertStructure('start')"
							data-cy="structure-insert-top-btn">
							<mat-icon class="tw-text-osee-green-9">add</mat-icon
							>Insert structure at start
						</button>
						<button
							mat-menu-item
							(click)="insertStructure()"
							data-cy="structure-insert-end-btn">
							<mat-icon class="tw-text-osee-green-9">add</mat-icon
							>Insert structure at end
						</button>
						<button
							mat-menu-item
							[matMenuTriggerFor]="copyMenu"
							[matMenuTriggerData]="{
								structure: structure,
							}"
							data-cy="structure-copy-btn">
							<mat-icon>content_copy</mat-icon>Copy
						</button>
					} @else {
						<mat-label>
							No options available for autogenerated structures.
						</mat-label>
					}
				}
			</ng-template>
		</mat-menu>
		<mat-menu #copyMenu>
			<ng-template
				matMenuContent
				let-structure="structure">
				<button
					mat-menu-item
					data-cy="element-insert-after-btn"
					(click)="copyStructure(structure(), structure().id)">
					<mat-icon class="tw-text-osee-green-9">add</mat-icon>Insert
					structure after
				</button>
				<button
					mat-menu-item
					data-cy="element-insert-top-btn"
					(click)="copyStructure(structure(), 'start')">
					<mat-icon class="tw-text-osee-green-9">add</mat-icon>Insert
					structure at top
				</button>
				<button
					mat-menu-item
					data-cy="element-insert-end-btn"
					(click)="copyStructure(structure())">
					<mat-icon class="tw-text-osee-green-9">add</mat-icon>Insert
					structure at end
				</button>
			</ng-template>
		</mat-menu>`,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StructureMenuComponent {
	menuData = model.required<{
		x: string;
		y: string;
		structure: structure;
		url: string;
		header:
			| keyof displayableStructureFields
			| ' '
			| 'txRate'
			| 'publisher'
			| 'subscriber'
			| 'messageNumber'
			| 'messagePeriodicity';
		isInDiff: boolean;
		open: boolean;
	}>();
	protected open = writableSlice(this.menuData, 'open');
	protected menuPositionX = computed(() => this.menuData().x);
	protected menuPositionY = computed(() => this.menuData().y);
	protected matMenuTrigger = viewChild.required(MatMenuTrigger);
	private _openMenu = effect(
		() => {
			if (this.open()) {
				this.matMenuTrigger().openMenu();
			}
		},
		{ allowSignalWrites: true }
	);
	protected structure = computed(() => this.menuData().structure);
	protected url = computed(() => this.menuData().url);
	isEditing = input.required<boolean>();
	protected header = computed(() => this.menuData().header);
	breadCrumb = input.required<string>();
	private dialog = inject(MatDialog);
	private structureService = inject(STRUCTURE_SERVICE_TOKEN);

	protected structureId = toSignal(this.structureService.singleStructureId, {
		initialValue: '',
	});
	protected singleStructureAvailable = computed(() => {
		const structureId = this.structureId();
		if (structureId !== '') {
			return !this.url().includes(structureId);
		}
		return true;
	});
	private _submessageId = toObservable(this.structureService.SubMessageId);
	protected canDisplayViewDiff = computed(() => {
		const changes = this.structure().changes;
		if (changes) {
			const header = this.header();
			if (
				header !== ' ' &&
				header !== 'txRate' &&
				header !== 'subscriber' &&
				header !== 'publisher' &&
				header !== 'messageNumber' &&
				header !== 'messagePeriodicity' &&
				header !== 'sizeInBytes' &&
				header !== 'numElements' &&
				header !== 'bytesPerSecondMaximum' &&
				header !== 'bytesPerSecondMinimum' &&
				header !== 'incorrectlySized' &&
				header !== 'autogenerated'
			) {
				const change = changes[header];
				if (change !== undefined) {
					return true;
				}
			}
		}
		return false;
	});
	protected copyStructure(
		structure: structure | structureWithChanges,
		afterStructure?: string
	) {
		this.dialog
			.open(AddStructureDialogComponent, {
				data: {
					id: this.structureService.SubMessageId(),
					name: this.breadCrumb(),
					structure: structuredClone(structure),
				},
				minHeight: '80vh',
				minWidth: '80vw',
			})
			.afterClosed()
			.pipe(
				take(1),
				filter((val) => val !== undefined),
				switchMap((result) =>
					this.structureService.copyStructure(
						result.structure,
						afterStructure
					)
				)
			)
			.subscribe();
	}
	protected insertStructure(afterStructure?: string) {
		this.dialog
			.open(AddStructureDialogComponent, {
				data: {
					id: this.structureService.subMessageId,
					name: this.breadCrumb(),
					structure: {
						id: '-1',
						gammaId: '-1',
						name: {
							id: '-1',
							typeId: '1152921504606847088',
							gammaId: '-1',
							value: '',
						},
						nameAbbrev: {
							id: '-1',
							typeId: '8355308043647703563',
							gammaId: '-1',
							value: '',
						},
						description: {
							id: '-1',
							typeId: '1152921504606847090',
							gammaId: '-1',
							value: '',
						},
						interfaceMaxSimultaneity: {
							id: '-1',
							typeId: '2455059983007225756',
							gammaId: '-1',
							value: '',
						},
						interfaceMinSimultaneity: {
							id: '-1',
							typeId: '2455059983007225755',
							gammaId: '-1',
							value: '',
						},
						interfaceTaskFileType: {
							id: '-1',
							typeId: '2455059983007225760',
							gammaId: '-1',
							value: 0,
						},
						interfaceStructureCategory: {
							id: '-1',
							typeId: '2455059983007225764',
							gammaId: '-1',
							value: '',
						},
						applicability: applicabilitySentinel,
						elements: [],
					},
				},
				minWidth: '80vw',
			})
			.afterClosed()
			.pipe(
				take(1),
				filter((val) => val !== undefined),
				switchMap((value: AddStructureDialog) =>
					value.structure.id !== '-1' && value.structure.id.length > 0
						? this.structureService.relateStructure(
								value.structure.id,
								afterStructure
							)
						: this.structureService.createStructure(
								value.structure,
								afterStructure
							)
				)
			)
			.subscribe();
	}
	protected deleteStructureDialog() {
		const id = this.structure().id;
		const name = this.structure().name.value;
		this.dialog
			.open(DeleteStructureDialogComponent, {
				data: {
					structureId: id,
					structureName: name,
				},
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((dialogResult: string) =>
					iif(
						() => dialogResult === 'ok',
						this.structureService.deleteStructure(id),
						of()
					)
				)
			)
			.subscribe();
	}
	protected removeStructureDialog() {
		const id = this.structure().id;
		const name = this.structure().name.value;
		this._submessageId
			.pipe(
				take(1),
				switchMap((subMessageId) =>
					this.dialog
						.open(RemoveStructureDialogComponent, {
							data: {
								subMessageId: subMessageId,
								structureId: id,
								structureName: name,
							},
						})
						.afterClosed()
						.pipe(
							take(1),
							switchMap((dialogResult: string) =>
								iif(
									() => dialogResult === 'ok',
									this.structureService.removeStructureFromSubmessage(
										id,
										subMessageId
									),
									of()
								)
							)
						)
				)
			)
			.subscribe();
	}
	protected viewDiff(open: boolean) {
		const changes = this.structure().changes;
		if (changes) {
			const header = this.header();
			if (
				header !== ' ' &&
				header !== 'txRate' &&
				header !== 'subscriber' &&
				header !== 'publisher' &&
				header !== 'messageNumber' &&
				header !== 'messagePeriodicity' &&
				header !== 'sizeInBytes' &&
				header !== 'numElements' &&
				header !== 'bytesPerSecondMaximum' &&
				header !== 'bytesPerSecondMinimum' &&
				header !== 'incorrectlySized' &&
				header !== 'autogenerated'
			) {
				const change = changes[header];
				if (change !== undefined) {
					this.structureService.sideNav = {
						opened: open,
						field: this.header(),
						currentValue: change.currentValue as
							| string
							| number
							| applic,
						previousValue: change.previousValue as
							| string
							| number
							| applic
							| undefined,
						transaction: change.transactionToken,
					};
				}
			}
		}
	}
	protected openDescriptionDialog() {
		const previousStructure = structuredClone(this.structure());
		this.dialog
			.open(EditViewFreeTextFieldDialogComponent, {
				data: {
					original: structuredClone(
						this.structure().description.value
					),
					type: 'Description',
					return: this.structure().description.value,
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
						this.structureService.partialUpdateStructure(
							{
								...this.structure(),
								description: {
									...this.structure().description,
									value: (response as EditViewFreeTextDialog)
										.return,
								},
							},
							previousStructure
						)
					)
				)
			)
			.subscribe();
	}
}
